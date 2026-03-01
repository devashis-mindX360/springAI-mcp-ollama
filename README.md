# Spring AI MCP Employee Server

A **Model Context Protocol (MCP)** server built with **Spring Boot 3.5** and **Spring AI 1.1**, exposing HR Employee data operations as tools that [Claude Desktop](https://claude.ai/download) can invoke in real time. The server communicates over **STDIO** (stdin/stdout) using the JSON-RPC–based MCP protocol — no REST endpoints, no HTTP.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Workflow Diagram](#workflow-diagram)
- [How It Works – Step by Step](#how-it-works--step-by-step)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Available Tools](#available-tools)
- [Database & Seed Data](#database--seed-data)
- [Prerequisites](#prerequisites)
- [Build & Run](#build--run)
- [Claude Desktop Configuration](#claude-desktop-configuration)
- [Example Prompts](#example-prompts)
- [Key Configuration Properties](#key-configuration-properties)
- [Extending the Server](#extending-the-server)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                       Claude Desktop (Client)                    │
│                                                                  │
│  User asks: "Who in Engineering earns more than $90,000?"        │
└──────────────┬──────────────────────────────────┬────────────────┘
               │  stdin (JSON-RPC request)        │  stdout (JSON-RPC response)
               ▼                                  ▲
┌──────────────────────────────────────────────────────────────────┐
│              Spring AI MCP Server (this project)                 │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │  STDIO Transport Layer (auto-configured)                │     │
│  │  • Reads JSON-RPC messages from stdin                   │     │
│  │  • Writes JSON-RPC responses to stdout                  │     │
│  └────────────────────────┬────────────────────────────────┘     │
│                           │                                      │
│  ┌────────────────────────▼────────────────────────────────┐     │
│  │  MCP Protocol Handler (McpSyncServer)                   │     │
│  │  • Handles initialize / tools/list / tools/call         │     │
│  │  • Routes tool calls to registered ToolCallbacks        │     │
│  └────────────────────────┬────────────────────────────────┘     │
│                           │                                      │
│  ┌────────────────────────▼────────────────────────────────┐     │
│  │  ToolCallbackProvider (McpConfig)                       │     │
│  │  • Scans @Tool-annotated methods in EmployeeTools       │     │
│  │  • Auto-generates JSON Schema for each tool's params    │     │
│  │  • Registers all tools with the MCP server              │     │
│  └────────────────────────┬────────────────────────────────┘     │
│                           │                                      │
│  ┌────────────────────────▼────────────────────────────────┐     │
│  │  EmployeeTools (9 @Tool-annotated methods)              │     │
│  │  → EmployeeService (business logic + transactions)      │     │
│  │  → EmployeeRepository (Spring Data JPA)                 │     │
│  │  → H2 In-Memory Database (employees table)              │     │
│  └─────────────────────────────────────────────────────────┘     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Workflow Diagram

Below is the end-to-end flow from user prompt to final answer:

```
┌─────────┐         ┌────────────────┐         ┌─────────────────────────┐
│  User   │         │ Claude Desktop │         │  MCP Server (Java JAR)  │
│         │         │   (AI Client)  │         │  springAI-mcp-1.0.0.jar │
└────┬────┘         └───────┬────────┘         └────────────┬────────────┘
     │                      │                               │
     │  1. Ask question     │                               │
     │  "Show all active    │                               │
     │   employees"         │                               │
     │─────────────────────►│                               │
     │                      │                               │
     │                      │  2. Spawn JAR process         │
     │                      │     (on first use)            │
     │                      │──────────────────────────────►│
     │                      │                               │
     │                      │  3. MCP Handshake             │
     │                      │  ── initialize ──────────────►│
     │                      │  ◄── serverInfo + caps ───────│
     │                      │                               │
     │                      │  4. Discover tools            │
     │                      │  ── tools/list ──────────────►│
     │                      │  ◄── [9 tool definitions] ────│
     │                      │     (name, description,       │
     │                      │      JSON Schema for params)  │
     │                      │                               │
     │                      │  5. AI decides to call tool   │
     │                      │  ── tools/call ──────────────►│
     │                      │     { tool: "getEmployees     │
     │                      │       ByActiveStatus",        │
     │                      │       args: {isActive: true}} │
     │                      │                               │
     │                      │          ┌────────────────────┤
     │                      │          │ 6. Internal flow:  │
     │                      │          │ EmployeeTools      │
     │                      │          │  → EmployeeService │
     │                      │          │  → JpaRepository   │
     │                      │          │  → H2 SQL query    │
     │                      │          │  → Return results  │
     │                      │          └────────────────────┤
     │                      │                               │
     │                      │  7. Tool response             │
     │                      │  ◄── [{employee data}] ───────│
     │                      │                               │
     │  8. Natural language  │                               │
     │     formatted answer │                               │
     │◄─────────────────────│                               │
     │                      │                               │
     │  "Here are the 21    │                               │
     │   active employees:  │                               │
     │   Alice Johnson..."  │                               │
     │                      │                               │
```

### Sequence Summary

| Step | Actor | Action |
|------|-------|--------|
| 1 | **User** | Types a natural language question in Claude Desktop |
| 2 | **Claude Desktop** | Spawns the MCP server JAR as a child process (first use only) |
| 3 | **Claude ↔ Server** | MCP handshake — `initialize` request, server responds with name, version, capabilities |
| 4 | **Claude ↔ Server** | `tools/list` — server returns all 9 tool definitions with JSON Schemas |
| 5 | **Claude → Server** | AI selects the best tool and sends a `tools/call` request with arguments |
| 6 | **Server (internal)** | EmployeeTools → EmployeeService → EmployeeRepository → H2 database |
| 7 | **Server → Claude** | Returns structured JSON data over stdout |
| 8 | **Claude → User** | Formats the raw data into a human-readable natural language response |

---

## How It Works – Step by Step

### 1. Startup & Auto-Configuration

When Claude Desktop launches the JAR, Spring Boot starts with `web-application-type=none` (no HTTP server). The **Spring AI MCP Server Starter** auto-configures:

- **STDIO Transport** — reads JSON-RPC from `stdin`, writes to `stdout`
- **McpSyncServer** — handles the MCP protocol lifecycle
- **Tool Registry** — discovers all `ToolCallbackProvider` beans and registers their tools

### 2. Tool Discovery

`McpConfig` declares a `MethodToolCallbackProvider` bean that wraps `EmployeeTools`. At startup, it:

1. Reflectively scans `EmployeeTools` for methods annotated with `@Tool`
2. Builds a `ToolCallback` for each method (name, description, method handle)
3. Auto-generates a **JSON Schema** from method parameters (using `-parameters` compiler flag + `@ToolParam` annotations)

### 3. MCP Handshake

Claude Desktop sends an `initialize` request. The server responds with:
- Server name: `employee-mcp-server`
- Server version: `1.0.0`
- Capabilities: list of 9 available tools with full schemas

### 4. Tool Invocation

When the user asks a question, Claude's AI:
1. Matches the intent to the best tool based on tool descriptions
2. Extracts parameters from the natural language query
3. Sends a `tools/call` JSON-RPC request over stdin
4. Receives the structured response over stdout
5. Formats the data into a human-readable answer

### 5. Data Layer

The server uses an **H2 in-memory database** seeded with 25 employees across 7 departments on every startup via `data.sql`. The JPA stack is:

```
EmployeeTools → EmployeeService → EmployeeRepository → H2 Database
   (@Tool)        (@Service)       (JpaRepository)     (in-memory)
```

---

## Project Structure

```
springAI-mcp/
├── pom.xml                          # Maven build (Spring Boot 3.5 + Spring AI 1.1)
├── claude_desktop_config.json       # Sample Claude Desktop config
├── README.md                        # This file
├── logs/                            # Runtime logs (stdout kept clean for STDIO)
└── src/
    └── main/
        ├── java/com/mindx360/mcp/
        │   ├── SpringAiMcpApplication.java   # Entry point
        │   ├── config/
        │   │   └── McpConfig.java            # Registers @Tool methods with MCP
        │   ├── entity/
        │   │   └── Employee.java             # JPA entity (employees table)
        │   ├── repository/
        │   │   └── EmployeeRepository.java   # Spring Data JPA repository
        │   ├── service/
        │   │   └── EmployeeService.java      # Business logic layer
        │   └── tool/
        │       └── EmployeeTools.java        # 9 @Tool-annotated MCP tools
        └── resources/
            ├── application.properties        # Server + DB + logging config
            └── data.sql                      # Seed data (25 employees)
```

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 21 | Runtime (Microsoft OpenJDK) |
| **Spring Boot** | 3.5.7 | Application framework |
| **Spring AI** | 1.1.2 | MCP Server auto-configuration, `@Tool` annotations |
| **Spring Data JPA** | (managed) | ORM & repository abstraction |
| **H2 Database** | (managed) | In-memory SQL database |
| **Lombok** | 1.18.36 | Boilerplate reduction (getters, constructors, builder) |
| **Jackson** | (managed) | JSON serialization (including `LocalDate` via JSR-310 module) |
| **Maven** | 3.x | Build tool |

---

## Available Tools

The server exposes **9 tools** to Claude Desktop:

| # | Tool Method | Description | Parameters |
|---|---|---|---|
| 1 | `getAllEmployees` | Retrieve the full list of all employees | *none* |
| 2 | `getEmployeesByDepartment` | Get employees in a specific department | `department` (String) |
| 3 | `getEmployeesWithSalaryGreaterThan` | Find employees earning above a threshold | `amount` (Double) |
| 4 | `getEmployeesByActiveStatus` | Filter by active/inactive status | `isActive` (Boolean) |
| 5 | `getEmployeesJoinedAfter` | Find employees who joined after a date | `afterDate` (String, ISO-8601) |
| 6 | `searchEmployeesByName` | Search by partial name match | `keyword` (String) |
| 7 | `getHighEarnersInDepartment` | Department + salary combined filter | `department` (String), `minSalary` (Double) |
| 8 | `getEmployeeById` | Get a single employee by ID | `id` (Long) |
| 9 | `getTotalEmployeeCount` | Get total headcount | *none* |

---

## Database & Seed Data

The H2 in-memory database is recreated on every startup (`ddl-auto=create-drop`) and seeded with **25 employees** across **7 departments**:

| Department | Employees | Salary Range |
|---|---|---|
| Engineering | 5 | $78,000 – $102,000 |
| HR | 3 | $65,000 – $72,000 |
| Finance | 4 | $76,000 – $94,000 |
| Marketing | 3 | $67,000 – $73,000 |
| Product | 3 | $85,000 – $98,000 |
| Operations | 2 | $59,000 – $62,000 |
| Data Science | 3 | $99,000 – $110,000 |
| DevOps | 2 | $89,000 – $96,000 |

> 4 employees are marked as **inactive** (isActive = false).

---

## Prerequisites

- **Java 21** (Microsoft OpenJDK recommended)
- **Maven 3.x**
- **Claude Desktop** (Windows / macOS)

---

## Build & Run

### 1. Build the FAT JAR

```bash
cd D:\MindX360\MCP\springAI-mcp
mvn clean package -DskipTests
```

This produces `target/springAI-mcp-1.0.0.jar`.

### 2. Test Locally (optional)

```bash
java -jar target/springAI-mcp-1.0.0.jar
```

> The server will start in STDIO mode and wait for JSON-RPC input on stdin. Press `Ctrl+C` to stop.

### 3. Configure Claude Desktop

See the [Claude Desktop Configuration](#claude-desktop-configuration) section below.

---

## Claude Desktop Configuration

Add the server entry to your Claude Desktop config file:

**Config file locations:**

| Platform | Path |
|---|---|
| Windows (Store app) | `%LOCALAPPDATA%\Packages\Claude_*\LocalCache\Roaming\Claude\claude_desktop_config.json` |
| Windows (Standard) | `%APPDATA%\Claude\claude_desktop_config.json` |
| macOS | `~/Library/Application Support/Claude/claude_desktop_config.json` |

**Configuration:**

```json
{
  "mcpServers": {
    "employee-mcp-server": {
      "command": "C:\\Users\\<YOUR_USERNAME>\\.jdks\\ms-21.0.10\\bin\\java.exe",
      "args": [
        "-jar",
        "D:\\MindX360\\MCP\\springAI-mcp\\target\\springAI-mcp-1.0.0.jar"
      ]
    }
  }
}
```

> **Important:** Replace `<YOUR_USERNAME>` with your actual Windows username and adjust the Java path to match your JDK 21 installation.

After saving, **restart Claude Desktop** for the changes to take effect. You should see the MCP server icon (hammer 🔨) in the chat input area.

---

## Example Prompts

Once configured, try these prompts in Claude Desktop:

| Prompt | Tool Invoked |
|---|---|
| "Show me all employees" | `getAllEmployees` |
| "Who works in Engineering?" | `getEmployeesByDepartment` |
| "List employees earning more than $90,000" | `getEmployeesWithSalaryGreaterThan` |
| "Show active employees" | `getEmployeesByActiveStatus` |
| "Who joined after January 2023?" | `getEmployeesJoinedAfter` |
| "Find employees named Alice" | `searchEmployeesByName` |
| "Who in Data Science earns above $100K?" | `getHighEarnersInDepartment` |
| "Get details for employee #3" | `getEmployeeById` |
| "How many employees do we have?" | `getTotalEmployeeCount` |

---

## Key Configuration Properties

| Property | Value | Purpose |
|---|---|---|
| `spring.main.web-application-type` | `none` | Disables HTTP server (STDIO only) |
| `spring.main.banner-mode` | `off` | Prevents banner from corrupting JSON-RPC |
| `spring.ai.mcp.server.stdio` | `true` | Enables STDIO transport |
| `spring.ai.mcp.server.name` | `employee-mcp-server` | Server identity in MCP handshake |
| `logging.pattern.console` | *(empty)* | Suppresses ALL console output (critical for STDIO) |
| `logging.file.name` | `./logs/mcp-server.log` | Redirects logs to file only |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Recreates schema on each startup |

> **Critical:** Any output on stdout/stderr (logs, banners, errors) will corrupt the JSON-RPC stream and break the MCP connection. All logging is routed to a file.

---

## Extending the Server

### Adding a New Tool

1. Add a new method to `EmployeeTools.java` (or create a new tool class):

```java
@Tool(description = "Description for Claude to understand when to use this tool")
public String myNewTool(
        @ToolParam(description = "Parameter description") String param) {
    return employeeService.someMethod(param);
}
```

2. If using a new tool class, register it in `McpConfig.java`:

```java
@Bean
public ToolCallbackProvider myToolCallbackProvider(MyToolClass myTools) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(myTools)
            .build();
}
```

3. Rebuild: `mvn clean package -DskipTests`
4. Restart Claude Desktop

### Switching to MySQL

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employeedb
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Add MySQL driver to `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## Troubleshooting

| Issue | Solution |
|---|---|
| Claude Desktop doesn't show the MCP server | Verify config JSON syntax, check JAR path exists, restart Claude Desktop |
| "Connection failed" error | Check `logs/mcp-server.log` for startup errors; ensure Java 21 is on the path |
| Tools not appearing | Ensure `-parameters` compiler flag is set in `pom.xml`; rebuild with `mvn clean package` |
| Stale data | The H2 database is in-memory — data resets on every restart (by design) |
| Logs corrupting STDIO | Ensure `logging.pattern.console=` is empty and `spring.main.banner-mode=off` |

---

## License

This project is for demonstration and educational purposes.

---

*Built with ❤️ by MindX360 using Spring AI and the Model Context Protocol.*
