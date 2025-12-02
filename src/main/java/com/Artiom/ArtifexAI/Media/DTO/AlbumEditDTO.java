package com.Artiom.ArtifexAI.Media.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumEditDTO {
    private String albumId;
    private String name;
}
