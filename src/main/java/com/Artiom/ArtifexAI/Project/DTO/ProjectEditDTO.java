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
public class ProjectEditDTO {
    private Long projectId;
    private String projectName;
    private ArtStyle artStyle;
}
