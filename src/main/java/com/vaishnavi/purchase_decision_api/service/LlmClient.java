package com.vaishnavi.purchase_decision_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaishnavi.purchase_decision_api.enums.Verdict;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String generateExplanation(String prompt) throws Exception  {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages",
                List.of(Map.of("role", "user", "content", prompt)
                ));

        String response = restClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    public String explainDecision(String itemName, BigDecimal price,
                                  Verdict verdict, int score, BigDecimal disposableIncome) {

        String prompt = buildPrompt(itemName, price, verdict, score, disposableIncome);

        try {
            // try the AI
            return generateExplanation(prompt);
        } catch (Exception e) {
            // AI failed (network, rate limit, bad response) — use rule-based fallback
            System.out.println("LLM CALL FAILED: " + e.getMessage());   // ← add this
            e.printStackTrace();
            return buildFallbackExplanation(itemName, price, verdict, score, disposableIncome);
        }

    }

    public String buildPrompt (String itemName, BigDecimal price,
                                Verdict verdict, int score, BigDecimal disposableIncome){
        return "A user wants to buy '" + itemName + "' for ₹" + price + ". " +
                "Their monthly disposable income is ₹" + disposableIncome + ". " +
                "The affordability verdict is " + verdict + " with a score of " + score + "/100. " +
                "Explain this verdict in 2-3 friendly, non-judgmental sentences to help them understand. " +
                "Do not repeat the numbers back mechanically — give practical, warm advice.";
    }

    public String buildFallbackExplanation(String itemName, BigDecimal price,
                                           Verdict verdict, int score, BigDecimal disposableIncome){
        return  "Based on your finances, buying " + itemName + " for ₹" + price +
                " scored " + score + "/100, giving a verdict of " + verdict +
                ". This is measured against your monthly disposable income of ₹" + disposableIncome + ".";
    }

}
