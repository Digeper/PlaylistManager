package org.muzika.playlistmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to create a new playlist")
public class CreatePlaylistRequest {
    @Schema(description = "Playlist name (required)", example = "My Favorite Songs", required = true)
    private String name;
    
    @Schema(description = "Playlist description (optional)", example = "A collection of my favorite tracks")
    private String description;
}


