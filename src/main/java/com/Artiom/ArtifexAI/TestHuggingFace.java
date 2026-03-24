package com.Artiom.ArtifexAI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class TestHuggingFace {

    private static final String API_URL =
            "https://router.huggingface.co/fal-ai/fal-ai/qwen-image-edit-plus?_subdomain=queue";

    private static final String API_KEY = "";

    public static void main(String[] args) {
        try {
            if (API_KEY == null || API_KEY.isEmpty()) {
                throw new RuntimeException("Please set HF_TOKEN environment variable!");
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            ObjectMapper mapper = new ObjectMapper();

            String requestBody = """
                    {
                      "prompt": "Add color to the picture.",
                      "image_urls": [
                        "https://fastly.picsum.photos/id/467/512/512.jpg?hmac=Udg-MdWheoPZUE8UsqOugR_GNhR-0jTrGgQL9izgy5Q"
                      ]
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("Initial response:");
            System.out.println(response.body());

            JsonNode root = mapper.readTree(response.body());

            if (!root.has("status_url") || !root.has("response_url")) {
                System.out.println("Invalid response:");
                System.out.println(response.body());
                return;
            }

            String statusUrl = root.get("status_url").asText();
            String responseUrl = root.get("response_url").asText();

            System.out.println("\n Polling for result...");

            int maxAttempts = 30;
            int attempt = 0;

            while (attempt < maxAttempts) {
                attempt++;
                Thread.sleep(2000);

                HttpRequest statusRequest = HttpRequest.newBuilder()
                        .uri(URI.create(statusUrl))
                        .header("Authorization", "Bearer " + API_KEY)
                        .GET()
                        .build();

                HttpResponse<String> statusResponse = client.send(
                        statusRequest,
                        HttpResponse.BodyHandlers.ofString()
                );

                JsonNode statusJson = mapper.readTree(statusResponse.body());
                String status = statusJson.has("status")
                        ? statusJson.get("status").asText()
                        : "UNKNOWN";

                System.out.println("Attempt " + attempt + " → " + status);

                if ("COMPLETED".equals(status)) break;

                if ("FAILED".equals(status)) {
                    System.out.println("Job failed:");
                    System.out.println(statusResponse.body());
                    return;
                }
            }

            if (attempt >= maxAttempts) {
                System.out.println("Timeout waiting for result");
                return;
            }

            HttpRequest resultRequest = HttpRequest.newBuilder()
                    .uri(URI.create(responseUrl))
                    .header("Authorization", "Bearer " + API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> resultResponse = client.send(
                    resultRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("\n Final response:");
            System.out.println(resultResponse.body());

            JsonNode resultJson = mapper.readTree(resultResponse.body());

            String imageUrl = extractImageUrl(resultJson);

            if (imageUrl != null) {
                System.out.println("\n FINAL IMAGE URL:");
                System.out.println(imageUrl);
            } else {
                System.out.println(" Could not find image URL!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractImageUrl(JsonNode json) {
        try {
            if (json.has("images")) {
                return json.get("images").get(0).get("url").asText();
            }
            if (json.has("output") && json.get("output").has("images")) {
                return json.get("output").get("images").get(0).get("url").asText();
            }
            if (json.has("image")) {
                return json.get("image").get("url").asText();
            }
        } catch (Exception ignored) {}
        return null;
    }
}