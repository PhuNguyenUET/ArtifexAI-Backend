package com.Artiom.ArtifexAI.Media.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AlbumMediaId implements Serializable {
    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "media_id")
    private Long mediaId;
}