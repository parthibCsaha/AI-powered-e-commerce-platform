package com.backend.service;

import com.backend.dto.AiSearchResponse;
import com.backend.dto.ProductSearchFilter;
import com.backend.entity.Product;
import com.backend.repository.ProductRepository;
import com.backend.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

        @Value("${app.ai.groq.api-key}")
        private String apiKey;

        @Value("${app.ai.groq.model}")
        private String model;

        private final RestTemplate restTemplate;

        private final ProductRepository productRepository;

        private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

        public AiSearchResponse aiSearchProducts(String userQuery) {
                String prompt = buildPrompt(userQuery);
                String aiResponse = callGroqApi(prompt);

                log.debug("Raw AI response:\n{}", aiResponse);

                // Strip markdown code fences if present (e.g. ```json ... ```)
                String cleanedResponse = stripMarkdownCodeFences(aiResponse);

                ProductSearchFilter filter;
                try {
                        filter = JSON_MAPPER.readValue(cleanedResponse, ProductSearchFilter.class);
                } catch (Exception e) {
                        log.error("Failed to parse AI response as ProductSearchFilter:\n{}", cleanedResponse, e);
                        throw new RuntimeException(
                                        "AI returned an unparseable response. Please try rephrasing your query.", e);
                }

                log.debug("Parsed filter: {}", filter);

                Specification<Product> spec = ProductSpecification.fromFilter(filter);
                List<Product> products = productRepository.findAll(spec);

                String message = products.isEmpty()
                                ? "No products found matching your query. Try broadening your search."
                                : "Found " + products.size() + " product(s) matching your query.";

                return new AiSearchResponse(products, filter, message);
        }

        // ---- Groq API communication ----

        private String callGroqApi(String userPrompt) {
                String url = "https://api.groq.com/openai/v1/chat/completions";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                Map<String, Object> requestBody = Map.of(
                                "model", model,
                                "temperature", 0,
                                "messages", List.of(
                                                Map.of("role", "system", "content", SYSTEM_PROMPT),
                                                Map.of("role", "user", "content", userPrompt)));

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map<String, Object>> response;
                try {
                        @SuppressWarnings("unchecked")
                        var rawResponse = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate
                                        .postForEntity(url, request, Map.class);
                        response = rawResponse;
                } catch (Exception e) {
                        log.error("Failed to call Groq API", e);
                        throw new RuntimeException("AI service is currently unavailable. Please try again later.", e);
                }

                Map<String, Object> responseBody = response.getBody();

                if (responseBody == null) {
                        throw new RuntimeException("Empty response from AI service.");
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

                if (choices == null || choices.isEmpty()) {
                        throw new RuntimeException("AI returned no choices.");
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

                return message.get("content").toString();
        }

        // ---- Helpers ----

        private String stripMarkdownCodeFences(String response) {
                String trimmed = response.trim();
                // Remove ```json ... ``` or ``` ... ```
                if (trimmed.startsWith("```")) {
                        int firstNewline = trimmed.indexOf('\n');
                        if (firstNewline != -1) {
                                trimmed = trimmed.substring(firstNewline + 1);
                        }
                        if (trimmed.endsWith("```")) {
                                trimmed = trimmed.substring(0, trimmed.length() - 3);
                        }
                        return trimmed.trim();
                }
                return trimmed;
        }

        private String buildPrompt(String userQuery) {
                return """
                                Extract product search filters from this query: "%s"
                                """.formatted(userQuery);
        }

        // ---- System prompt for consistent AI behavior ----

        private static final String SYSTEM_PROMPT = """
                        You are a product search filter extractor for an e-commerce platform.
                        Given a user's natural language query, extract structured search filters.

                        Return ONLY valid JSON with this exact structure (no markdown, no explanation, no extra text):
                        {
                          "brand": string or null,
                          "productName": string or null,
                          "category": string or null,
                          "minPrice": number or null,
                          "maxPrice": number or null,
                          "minRating": number or null,
                          "keywords": ["keyword1", "keyword2"]
                        }

                        Rules:
                        - "brand": extract brand name if mentioned (e.g. "Nike", "Apple", "Samsung")
                        - "productName": extract specific product name if mentioned (e.g. "iPhone 15", "Air Max")
                        - "category": extract product category if mentioned (e.g. "shoes", "electronics", "clothing")
                        - "minPrice"/"maxPrice": extract price range. "under $100" → maxPrice=100. "above $50" → minPrice=50
                        - "minRating": extract minimum rating if mentioned. "highly rated" → 4.0, "top rated" → 4.5
                        - "keywords": extract descriptive words that help match product names/descriptions (colors, materials, features, etc.)

                        Examples:
                        Query: "cheap Nike running shoes"
                        {"brand":"Nike","productName":null,"category":"shoes","minPrice":null,"maxPrice":null,"minRating":null,"keywords":["running","cheap"]}

                        Query: "Samsung phones under $500 with good reviews"
                        {"brand":"Samsung","productName":null,"category":"phones","minPrice":null,"maxPrice":500,"minRating":4.0,"keywords":["good reviews"]}

                        Query: "red winter jacket between $50 and $150"
                        {"brand":null,"productName":null,"category":"jacket","minPrice":50,"maxPrice":150,"minRating":null,"keywords":["red","winter"]}
                        """;
}
