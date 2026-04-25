package com.Artiom.ArtifexAI.Media.DTO;

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
    private List<Long> mediaIds;
}
