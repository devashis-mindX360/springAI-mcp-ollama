package com.mindx360.mcp.config;

import com.mindx360.mcp.tool.ClientTools;
import com.mindx360.mcp.tool.EmployeeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient configuration.
 *
 * <p>Builds a singleton {@link ChatClient} that has
 * {@link EmployeeTools} and {@link ClientTools} registered as default tools.
 * Spring AI will automatically invoke their {@code @Tool}-annotated methods
 * when Ollama decides a tool call is necessary to fulfill a prompt.
 *
 * <p>Tools are registered via {@code defaultTools()} so every chat call made
 * through this client automatically has access to them without requiring
 * per-call registration.
 */
@Configuration
public class AiConfig {

    /**
     * Produces the application-scoped {@link ChatClient} wired to Ollama.
     *
     * @param builder       auto-configured by Spring AI Ollama starter
     * @param employeeTools bean exposing HR employee operations
     * @param clientTools   bean exposing client-related operations
     * @return fully configured {@link ChatClient}
     */
    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            EmployeeTools employeeTools,
            ClientTools clientTools) {

        return builder
                .defaultSystem("""
                        You are an HR assistant with access to a MySQL-backed company database.
                        When the user asks for data, call the appropriate tool to retrieve it.
                        Always return results in a clear, structured format.
                        """)
                .defaultTools(employeeTools, clientTools)
                .build();
    }
}
