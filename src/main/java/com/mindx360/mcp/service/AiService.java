package com.mindx360.mcp.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Application service that orchestrates AI-driven prompt execution.
 *
 * <p>Delegates to a pre-configured {@link ChatClient} that has
 * {@code @Tool}-annotated beans registered as default tools. When Ollama
 * determines that a tool call is required, Spring AI automatically invokes
 * the matching method, collects the result, and feeds it back into the
 * conversation – all transparently before returning the final response.
 *
 * <h2>Flow per request</h2>
 * <ol>
 *   <li>REST layer receives user prompt → calls {@link #chat(String)}.</li>
 *   <li>{@code ChatClient} sends the prompt to Ollama (llama3.1).</li>
 *   <li>If Ollama requests a tool call, Spring AI invokes the annotated
 *       method (e.g. {@code EmployeeTools#getAllEmployees()}).</li>
 *   <li>Tool result is serialised and appended to the conversation.</li>
 *   <li>Ollama produces a final text response, which is returned.</li>
 * </ol>
 *
 * <p>No manual JSON parsing, no raw SQL – all data access is domain-driven
 * through the service → repository layers.
 */
@Service
public class AiService {

    private final ChatClient chatClient;

    /**
     * Constructor injection – {@code ChatClient} is configured in
     * {@link com.mindx360.mcp.config.AiConfig}.
     *
     * @param chatClient pre-built client with tools and system prompt registered
     */
    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Sends a user prompt to Ollama and returns the model's final response.
     *
     * <p>Spring AI handles the full tool-calling loop internally; this method
     * always returns the final, human-readable answer.
     *
     * @param prompt the user's natural-language query
     * @return AI-generated response string
     * @throws IllegalArgumentException if {@code prompt} is null or blank
     */
    public String chat(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be null or blank");
        }

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
