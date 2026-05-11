package com.Artiom.ArtifexAI.FalAI;

import java.util.List;

public interface FalAIService {

    byte[] generateImageFlux(String prompt);

    byte[] editImageFlux(String prompt, List<String> imageDataUris);

    byte[] generateImageQwen(String prompt);

    byte[] editImageQwen(String prompt, List<String> imageDataUris);


    byte[] generateImageGPT(String prompt);

    byte[] editImageGPT(String prompt, List<String> imageDataUris);
}

