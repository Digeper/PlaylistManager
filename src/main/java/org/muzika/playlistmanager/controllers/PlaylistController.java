package org.muzika.playlistmanager.controllers;

import org.muzika.playlistmanager.dto.CreatePlaylistRequest;
import org.muzika.playlistmanager.dto.PlaylistResponse;
import org.muzika.playlistmanager.dto.PlaylistWithSongsResponse;
import org.muzika.playlistmanager.services.PlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new IllegalStateException("User not authenticated");
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> getAllPlaylists() {
        String username = getAuthenticatedUsername();
        List<PlaylistResponse> playlists = playlistService.getAllPlaylists(username);
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistWithSongsResponse> getPlaylist(@PathVariable UUID id) {
        String username = getAuthenticatedUsername();
        PlaylistWithSongsResponse response = playlistService.getPlaylistSongs(id, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(@RequestBody CreatePlaylistRequest request) {
        String username = getAuthenticatedUsername();
        PlaylistResponse response = playlistService.createPlaylist(username, request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/song/{songId}")
    public ResponseEntity<Void> addSongToPlaylist(
            @PathVariable UUID id,
            @PathVariable UUID songId) {
        String username = getAuthenticatedUsername();
        playlistService.addSongToPlaylist(id, songId, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable UUID id) {
        String username = getAuthenticatedUsername();
        playlistService.deletePlaylist(id, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/song/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable UUID id,
            @PathVariable UUID songId) {
        String username = getAuthenticatedUsername();
        playlistService.removeSongFromPlaylist(id, songId, username);
        return ResponseEntity.noContent().build();
    }
}


