package com.Artiom.ArtifexAI.Project.DTO;

import com.Artiom.ArtifexAI.Project.Model.ArtStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateDTO {
    private String projectName;
    private String instructions;
    private ArtStyle artStyle;
}
