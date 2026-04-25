package com.Artiom.ArtifexAI.Media.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "album_media")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumMedia {
    @EmbeddedId
    private AlbumMediaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("albumId")
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private Media media;
}