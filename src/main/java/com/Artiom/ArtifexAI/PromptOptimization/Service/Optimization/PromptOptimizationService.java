package com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization;

import java.util.List;

public interface PromptOptimizationService {
    String optimizePrompt(String prompt);

    List<String> optimizeInstruction(String instruction);

    String analyzePromptAndImages(String prompt, List<byte[]> imageData, List<String> instructions);
}
