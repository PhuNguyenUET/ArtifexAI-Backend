package com.Artiom.ArtifexAI.Project.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInstructionUpdateDTO {
    private Long projectId;
    private List<String> instructions;
    private String newInstruction;
}
