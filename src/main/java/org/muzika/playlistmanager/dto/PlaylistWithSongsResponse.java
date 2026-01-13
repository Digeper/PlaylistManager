package org.muzika.playlistmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Playlist with all its songs")
public class PlaylistWithSongsResponse {
    @Schema(description = "Playlist information", required = true)
    private PlaylistResponse playlist;
    
    @Schema(description = "List of songs in playlist, ordered by position", required = true)
    private List<PlaylistSongResponse> songs;
}


