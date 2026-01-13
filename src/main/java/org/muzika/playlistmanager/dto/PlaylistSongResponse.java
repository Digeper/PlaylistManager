package org.muzika.playlistmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Song information within a playlist")
public class PlaylistSongResponse {
    @Schema(description = "Song UUID (reference to QueueManager Song)", example = "770e8400-e29b-41d4-a716-446655440002", required = true)
    private UUID songId;
    
    @Schema(description = "Position of song in playlist (0-based)", example = "0", required = true)
    private Integer position;
    
    @Schema(description = "Timestamp when song was added to playlist", example = "2024-01-10T10:00:00Z", required = true)
    private LocalDateTime addedAt;
}


