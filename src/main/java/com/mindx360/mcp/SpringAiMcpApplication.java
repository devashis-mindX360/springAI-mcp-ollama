package com.mindx360.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring AI MCP Employee Server.
 *
 * <p>Spring AI MCP Server Starter auto-configures:
 * <ul>
 *   <li>STDIO transport handler (reads from stdin, writes to stdout)</li>
 *   <li>JSON-RPC message dispatcher</li>
 *   <li>MCP tool registry – discovers all ToolCallbackProvider beans</li>
 *   <li>JSON Schema generation for each @Tool-annotated method</li>
 * </ul>
 *
 * <p>No manual McpRunner, JSON parsing, or tool registration is needed.
 */
@SpringBootApplication
public class SpringAiMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiMcpApplication.class, args);
    }
}
