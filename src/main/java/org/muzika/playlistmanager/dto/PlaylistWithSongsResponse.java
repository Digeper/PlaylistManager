package org.muzika.playlistmanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlaylistWithSongsResponse {
    private PlaylistResponse playlist;
    private List<PlaylistSongResponse> songs;
}


