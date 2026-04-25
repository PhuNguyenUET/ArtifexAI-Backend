package com.Artiom.ArtifexAI.HuggingFace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class HuggingFaceServiceImpl implements HuggingFaceService {

    @Value("${huggingface.apiKey}")
    private String apiKey;

    @Value("${huggingface.flux2.generateUrl}")
    private String flux2GenerateUrl;

    @Value("${huggingface.flux2.editUrl}")
    private String flux2EditUrl;

    @Value("${huggingface.qwen.generateUrl}")
    private String qwenGenerateUrl;

    @Value("${huggingface.qwen.editUrl}")
    private String qwenEditUrl;

    @Value("${huggingface.firered.editUrl}")
    private String fireRedEditUrl;

    @Value("${huggingface.pollIntervalMs:5000}")
    private long pollIntervalMs;

    @Value("${huggingface.maxPollAttempts:150}")
    private int maxPollAttempts;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] generateImage(String prompt) {
        log.info("[Flux2] generateImage - prompt length={}", prompt.length());
        return callHuggingFaceAsync("Flux2", flux2GenerateUrl, prompt, Collections.emptyList());
    }

    @Override
    public byte[] editImage(String prompt, List<String> imageDataUris) {
        log.info("[Flux2] editImage - prompt length={}, images={}", prompt.length(), imageDataUris.size());
        return callHuggingFaceAsync("Flux2", flux2EditUrl, prompt, imageDataUris);
    }

    @Override
    public byte[] editImageQwen(String prompt, List<String> imageDataUris) {
        log.info("[Qwen] editImage - prompt length={}, images={}", prompt.length(), imageDataUris.size());
        return callHuggingFaceAsync("Qwen", qwenEditUrl, prompt, imageDataUris);
    }

    @Override
    public byte[] generateImageQwen(String prompt) {
        log.info("[Qwen] generateImage - prompt length={}", prompt.length());
        return callHuggingFaceAsync("Qwen", qwenGenerateUrl, prompt, Collections.emptyList());
    }

    @Override
    public byte[] editImageFireRed(String prompt, List<String> imageDataUris) {
        log.info("[FireRed] editImage - prompt length={}, images={}", prompt.length(), imageDataUris.size());
        return callHuggingFaceAsync("FireRed", fireRedEditUrl, prompt, imageDataUris);
    }


    private byte[] callHuggingFaceAsync(String modelTag, String endpointUrl, String prompt, List<String> imageDataUris) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("prompt", prompt);

            if (imageDataUris != null && !imageDataUris.isEmpty()) {
                ArrayNode arr = body.putArray("image_urls");
                imageDataUris.forEach(arr::add);
            }

            String requestBody = objectMapper.writeValueAsString(body);
            log.debug("[{}] Request body (first 500 chars): {}", modelTag, requestBody.substring(0, Math.min(500, requestBody.length())));

            HttpRequest submitRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> submitResponse = httpClient.send(submitRequest, HttpResponse.BodyHandlers.ofString());

            log.info("[{}] Submit response status={}", modelTag, submitResponse.statusCode());
            log.debug("[{}] Submit response body={}", modelTag, submitResponse.body());

            JsonNode submitJson = objectMapper.readTree(submitResponse.body());

            if (!submitJson.has("status_url") || !submitJson.has("response_url")) {
                String error = submitJson.has("error") ? submitJson.get("error").asText() : submitResponse.body();
                throw new RuntimeException("[" + modelTag + "] Job submission failed: " + error);
            }

            String statusUrl = submitJson.get("status_url").asText();
            String responseUrl = submitJson.get("response_url").asText();

            log.info("[{}] Job submitted - statusUrl={}", modelTag, statusUrl);

            int attempt = 0;
            long startTime = System.currentTimeMillis();
            String lastStatus = "";
            while (attempt < maxPollAttempts) {
                attempt++;
                Thread.sleep(pollIntervalMs);

                HttpRequest statusRequest = HttpRequest.newBuilder()
                        .uri(URI.create(statusUrl))
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                HttpResponse<String> statusResponse = httpClient.send(statusRequest, HttpResponse.BodyHandlers.ofString());

                JsonNode statusJson = objectMapper.readTree(statusResponse.body());
                String status = statusJson.has("status") ? statusJson.get("status").asText() : "UNKNOWN";
                long elapsedSec = (System.currentTimeMillis() - startTime) / 1000;

                if (!status.equals(lastStatus)) {
                    log.info("[{}] Status changed to {} after {}s (attempt={})", modelTag, status, elapsedSec, attempt);
                    lastStatus = status;
                } else if ("IN_QUEUE".equals(status)) {
                    log.info("[{}] Still IN_QUEUE - elapsed={}s, attempt={}/{}", modelTag, elapsedSec, attempt, maxPollAttempts);
                } else {
                    log.info("[{}] Poll attempt={} status={} elapsed={}s", modelTag, attempt, status, elapsedSec);
                }

                if ("COMPLETED".equals(status)) break;

                if ("FAILED".equals(status) || "ERROR".equals(status)) {
                    String err = statusJson.has("error") ? statusJson.get("error").asText() : statusResponse.body();
                    throw new RuntimeException("[" + modelTag + "] Job failed: " + err);
                }
            }

            long totalSec = (System.currentTimeMillis() - startTime) / 1000;
            if (attempt >= maxPollAttempts) {
                throw new RuntimeException("[" + modelTag + "] Job timed out after " + totalSec + "s (" + maxPollAttempts + " attempts). Last status: " + lastStatus);
            }

            HttpRequest resultRequest = HttpRequest.newBuilder()
                    .uri(URI.create(responseUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> resultResponse = httpClient.send(resultRequest, HttpResponse.BodyHandlers.ofString());

            log.debug("[{}] Result body={}", modelTag, resultResponse.body());

            JsonNode resultJson = objectMapper.readTree(resultResponse.body());
            String imageUrl = extractImageUrl(modelTag, resultJson);

            if (imageUrl == null || imageUrl.isBlank()) {
                throw new RuntimeException("[" + modelTag + "] Result contained no image URL: " + resultResponse.body());
            }

            log.info("[{}] Downloading generated image from url={}", modelTag, imageUrl);
            return downloadBytes(modelTag, imageUrl);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("[" + modelTag + "] Call failed: " + e.getMessage(), e);
        }
    }

    private String extractImageUrl(String modelTag, JsonNode json) {
        try {
            if (json.has("images") && json.get("images").isArray() && !json.get("images").isEmpty()) {
                JsonNode first = json.get("images").get(0);
                if (first.has("url")) return first.get("url").asText();
            }
            if (json.has("output") && json.get("output").has("images")) {
                JsonNode first = json.get("output").get("images").get(0);
                if (first.has("url")) return first.get("url").asText();
            }
            if (json.has("image") && json.get("image").has("url")) {
                return json.get("image").get("url").asText();
            }
            if (json.has("url")) {
                return json.get("url").asText();
            }
        } catch (Exception ignored) {}
        log.warn("[{}] Could not parse image URL from result: {}", modelTag, json);
        return null;
    }

    private byte[] downloadBytes(String modelTag, String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to download image from " + url + " (status " + response.statusCode() + ")");
        }

        log.info("[{}] Downloaded {} bytes from {}", modelTag, response.body().length, url);
        return response.body();
    }
}
