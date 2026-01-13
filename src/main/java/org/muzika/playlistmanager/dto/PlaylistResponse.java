package org.muzika.playlistmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Playlist information")
public class PlaylistResponse {
    @Schema(description = "Playlist UUID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private UUID id;
    
    @Schema(description = "Owner user UUID", example = "660e8400-e29b-41d4-a716-446655440001", required = true)
    private UUID userId;
    
    @Schema(description = "Playlist name", example = "My Favorite Songs", required = true)
    private String name;
    
    @Schema(description = "Playlist description", example = "A collection of my favorite tracks")
    private String description;
    
    @Schema(description = "Playlist creation timestamp", example = "2024-01-01T12:00:00Z", required = true)
    private LocalDateTime createdAt;
    
    @Schema(description = "Playlist last update timestamp", example = "2024-01-15T14:30:00Z", required = true)
    private LocalDateTime updatedAt;
}


