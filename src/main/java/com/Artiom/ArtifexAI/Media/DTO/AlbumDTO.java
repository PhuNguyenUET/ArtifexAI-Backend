package com.Artiom.ArtifexAI.Media.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumDTO {
    private String id;
    private String name;
    private List<MediaDTO> mediaList;
    private Date createdDate;
    private Date modifiedDate;
}
