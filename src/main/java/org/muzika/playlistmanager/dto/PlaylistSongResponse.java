package org.muzika.playlistmanager.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlaylistSongResponse {
    private UUID songId;
    private Integer position;
    private LocalDateTime addedAt;
}


