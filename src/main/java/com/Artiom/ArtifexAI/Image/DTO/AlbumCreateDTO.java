package com.Artiom.ArtifexAI.Image.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumCreateDTO {
    private String name;
    private List<String> imageIds;
}
