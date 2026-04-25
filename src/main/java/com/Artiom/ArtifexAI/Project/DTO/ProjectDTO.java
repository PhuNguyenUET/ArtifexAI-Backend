package com.Artiom.ArtifexAI.Project.DTO;

import com.Artiom.ArtifexAI.Project.Model.ArtStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {
    private Long id;
    private String projectName;
    @Builder.Default
    private List<String> instructions = new ArrayList<>();
    private ArtStyle artStyle;
    private Date createdDate;
    private Date modifiedDate;
}
