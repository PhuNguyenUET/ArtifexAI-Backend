package com.Artiom.ArtifexAI.Media.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaDTO {
    private Long id;
    private String mediaPath;
    private String mediaUrl;
    private Date createdDate;
}
