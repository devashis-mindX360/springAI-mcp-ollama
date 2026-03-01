package com.mindx360.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring AI Ollama REST Backend.
 *
 * <p>Spring Boot auto-configures:
 * <ul>
 *   <li>Embedded Tomcat on port 8080 (standard web mode)</li>
 *   <li>Spring AI {@code ChatClient} wired to Ollama (llama3.1)</li>
 *   <li>Spring Data JPA connected to MySQL</li>
 * </ul>
 *
 * <p>The AI layer is driven by:
 * <ul>
 *   <li>{@code AiConfig} – builds the {@code ChatClient} with registered tools</li>
 *   <li>{@code AiService} – orchestrates prompt execution and tool-calling loop</li>
 *   <li>{@code AiController} – exposes {@code POST /ai/query}</li>
 * </ul>
 *
 * <p>No MCP protocol, no STDIO transport, no Claude Desktop dependency.
 */
@SpringBootApplication
public class SpringAiMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiMcpApplication.class, args);
    }
}

