package com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization;

import java.util.List;

public interface PromptOptimizationService {
    String optimizePrompt(String prompt);

    String optimizePromptForDiffusion(String prompt);

    String optimizeContextForDiffusion(String prompt, String context);

    List<String> optimizeInstruction(String instruction);

    List<String> optimizeInstructionUpdate(String instruction, List<String> existingInstructions);

    String analyzePromptAndImages(String prompt, List<byte[]> imageData, List<String> instructions);
}
