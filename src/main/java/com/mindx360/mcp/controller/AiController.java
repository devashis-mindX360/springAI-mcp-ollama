package com.mindx360.mcp.controller;

import com.mindx360.mcp.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller that exposes the AI query endpoint.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /ai/query} – accepts a plain-text or JSON prompt,
 *       delegates to {@link AiService}, and returns a JSON response.</li>
 * </ul>
 *
 * <h2>Request formats accepted</h2>
 * <pre>
 * // Plain text (Content-Type: text/plain)
 * How many employees are in the Engineering department?
 *
 * // JSON (Content-Type: application/json)
 * { "prompt": "List all active employees." }
 * </pre>
 *
 * <h2>Response format</h2>
 * <pre>
 * HTTP 200 OK
 * Content-Type: application/json
 * {
 *   "response": "There are 12 employees in the Engineering department..."
 * }
 * </pre>
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    /**
     * Constructor injection of the AI orchestration service.
     *
     * @param aiService service that drives ChatClient + Ollama tool-calling
     */
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * Accepts a user prompt and returns an AI-generated response.
     *
     * <p>Supports two content types:
     * <ul>
     *   <li>{@code text/plain} – body is treated as the raw prompt.</li>
     *   <li>{@code application/json} – body must be a JSON object with a
     *       {@code "prompt"} key: {@code { "prompt": "..." }}.</li>
     * </ul>
     *
     * @param request the raw request body (plain text or JSON string)
     * @return {@code 200 OK} with {@code { "response": "..." }} on success,
     *         or {@code 400 Bad Request} if the prompt is blank
     */
    @PostMapping(
            value = "/query",
            consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> query(@RequestBody String request) {
        String prompt = extractPrompt(request);

        if (prompt.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Prompt must not be blank"));
        }

        String response = aiService.chat(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Extracts the actual prompt text from either plain-text or a minimal
     * JSON envelope ({@code {"prompt":"..."}}).
     *
     * <p>Keeps the controller free of a hard Jackson dependency by using a
     * simple string heuristic – sufficient given the narrow contract.
     */
    private String extractPrompt(String body) {
        if (body == null) return "";
        String trimmed = body.trim();
        // Detect minimal JSON object with a "prompt" key
        if (trimmed.startsWith("{") && trimmed.contains("\"prompt\"")) {
            int colonIdx = trimmed.indexOf(':', trimmed.indexOf("\"prompt\""));
            if (colonIdx != -1) {
                String after = trimmed.substring(colonIdx + 1).trim();
                // Strip surrounding quotes and closing brace
                if (after.startsWith("\"")) {
                    int end = after.lastIndexOf('"');
                    if (end > 0) return after.substring(1, end);
                }
            }
        }
        return trimmed;
    }
}
