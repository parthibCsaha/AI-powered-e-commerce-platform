package com.backend.service;

import com.backend.dto.ProductSearchFilter;
import com.backend.entity.Product;
import com.backend.repository.ProductRepository;
import com.backend.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${app.ai.groq.api-key}")
    private String apiKey;

    @Value("${app.ai.groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ProductRepository productRepository;

    public List<Product> searchProducts(String userQuery) {

        String prompt = buildPrompt(userQuery);
        String aiResponse = askAI(prompt);

        System.out.println("AI Response: \n" + aiResponse);

        ProductSearchFilter filter;
        try {
            filter = objectMapper.readValue(aiResponse, ProductSearchFilter.class);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: \n" + aiResponse, e);
        }

        System.out.println("Parsed Filter: \n" + filter);

        Specification<Product> spec = ProductSpecification.fromFilter(filter);

        System.out.println(spec);

        return productRepository.findAll(spec);

    }

    public String askAI(String userPrompt) {

        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", userPrompt
                        )
                )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null) {
            throw new RuntimeException("Empty response from AI");
        }

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) responseBody.get("choices");

        Map<String, Object> message =
                (Map<String, Object>) choices.get(0).get("message");

        return message.get("content").toString();
    }

    private String buildPrompt(String userQuery) {
        return """
                Extract product filters from this user query.

                Return ONLY like this bellow structure:
                {
                  "category": string or null,
                  "color": string or null,
                  "minPrice": number or null,
                  "maxPrice": number or null,
                  "keywords": array of strings
                }

                User query:
                "%s"
                """.formatted(userQuery);
    }

}



