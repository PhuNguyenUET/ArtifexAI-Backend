package com.Artiom.ArtifexAI.FalAI;

import ai.fal.client.ClientConfig;
import ai.fal.client.CredentialsResolver;
import ai.fal.client.FalClient;
import ai.fal.client.SubscribeOptions;
import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FalAIServiceImpl implements FalAIService {
    @Value("${falai.apiKey}")
    private String falAiKey;

    @Value("${falai.flux.generateModel}")
    private String fluxGenerateModel;

    @Value("${falai.flux.editModel}")
    private String fluxEditModel;

    @Value("${falai.qwen.generateModel}")
    private String qwenGenerateModel;

    @Value("${falai.qwen.editModel}")
    private String qwenEditModel;


    @Value("${falai.gpt.generateModel}")
    private String gptGenerateModel;

    @Value("${falai.gpt.editModel}")
    private String gptEditModel;

    private FalClient falClient;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @PostConstruct
    private void init() {
        falClient = FalClient.withConfig(
                ClientConfig.withCredentials(CredentialsResolver.fromApiKey(falAiKey)));
    }

    @Override
    public byte[] generateImageFlux(String prompt) {
        return callFalAI("Flux-Generate", fluxGenerateModel, prompt, List.of());
    }

    @Override
    public byte[] editImageFlux(String prompt, List<String> imageDataUris) {
        return callFalAI("Flux-Edit", fluxEditModel, prompt, imageDataUris);
    }

    @Override
    public byte[] generateImageQwen(String prompt) {
        return callFalAI("Qwen-Generate", qwenGenerateModel, prompt, List.of());
    }

    @Override
    public byte[] editImageQwen(String prompt, List<String> imageDataUris) {
        return callFalAI("Qwen-Edit", qwenEditModel, prompt, imageDataUris);
    }


    @Override
    public byte[] generateImageGPT(String prompt) {
        return callFalAI("GPT-Image-2-Generate", gptGenerateModel, prompt, List.of());
    }

    @Override
    public byte[] editImageGPT(String prompt, List<String> imageDataUris) {
        return callFalAI("GPT-Image-2-Edit", gptEditModel, prompt, imageDataUris);
    }

    private byte[] callFalAI(String tag, String modelEndpoint, String prompt, List<String> imageUrls) {
        try {
            Map<String, Object> input = new HashMap<>();
            input.put("prompt", prompt);
            if (imageUrls != null && !imageUrls.isEmpty()) {
                input.put("image_urls", imageUrls);
            }

            var result = falClient.subscribe(modelEndpoint,
                    SubscribeOptions.<JsonObject>builder()
                            .input(input)
                            .resultType(JsonObject.class)
                            .build());

            JsonObject data = result.getData();
            String imageUrl = extractImageUrl(data);

            if (imageUrl == null || imageUrl.isBlank()) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "[" + tag + "] Result contained no image URL: " + data);
            }

            return downloadBytes(tag, imageUrl);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "[" + tag + "] fal-ai call failed: " + e.getMessage());
        }
    }

    private String extractImageUrl(JsonObject json) {
        try {
            if (json.has("images") && json.get("images").isJsonArray()) {
                JsonArray images = json.getAsJsonArray("images");
                if (!images.isEmpty()) {
                    JsonObject first = images.get(0).getAsJsonObject();
                    if (first.has("url")) return first.get("url").getAsString();
                }
            }
            if (json.has("image") && json.get("image").isJsonObject()) {
                JsonObject img = json.getAsJsonObject("image");
                if (img.has("url")) return img.get("url").getAsString();
            }
            if (json.has("url")) return json.get("url").getAsString();
        } catch (Exception ignored) {
        }
        return null;
    }

    private byte[] downloadBytes(String tag, String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(120))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY,
                    "[" + tag + "] Failed to download image from " + url + " (status " + response.statusCode() + ")");
        }

        return response.body();
    }
}
