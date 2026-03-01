package com.mindx360.mcp.config;

import com.mindx360.mcp.tool.ClientTools;
import com.mindx360.mcp.tool.EmployeeTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI MCP tool registration configuration.
 *
 * <h2>What this class does:</h2>
 * <p>Wraps the {@link EmployeeTools} bean in a
 * {@link MethodToolCallbackProvider}, which:
 * <ol>
 *   <li>Reflectively scans {@code EmployeeTools} for methods annotated
 *       with {@code @Tool}.</li>
 *   <li>Builds a {@code ToolCallback} for each such method, capturing
 *       the method handle, parameter types, and description text.</li>
 *   <li>Generates a JSON Schema for each tool's input parameters
 *       (using method parameter names preserved by the {@code -parameters}
 *       compiler flag and {@code @ToolParam} descriptions).</li>
 * </ol>
 *
 * <p>The Spring AI MCP Server auto-configuration then discovers every
 * {@code ToolCallbackProvider} bean and registers all callbacks with the
 * MCP {@code McpSyncServer} – so Claude Desktop receives the full tool
 * catalogue automatically on connection.
 *
 * <h2>What this class does NOT do:</h2>
 * <ul>
 *   <li>No manual JSON-RPC handling.</li>
 *   <li>No manual STDIO stream readers/writers.</li>
 *   <li>No manual {@code initialize} / {@code tools/list} response building.</li>
 * </ul>
 *
 * <p>To add more tool classes in future, simply add them as additional
 * {@code toolObjects()} arguments or create another {@code @Bean} method.
 */
@Configuration
public class McpConfig {

    /**
     * Registers all {@code @Tool}-annotated methods in {@link EmployeeTools}
     * with the Spring AI MCP Server as callable tool callbacks.
     *
     * @param employeeTools the Spring-managed {@link EmployeeTools} bean
     * @return a {@link ToolCallbackProvider} consumed by Spring AI MCP auto-config
     */
    @Bean
    ToolCallbackProvider employeeToolCallbacks(EmployeeTools tools) {  // renamed
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }

    @Bean
    ToolCallbackProvider clientToolCallbacks(ClientTools tools) {  // renamed
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }
}
