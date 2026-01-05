package org.muzika.playlistmanager.controllers;

import org.muzika.playlistmanager.dto.CreatePlaylistRequest;
import org.muzika.playlistmanager.dto.PlaylistResponse;
import org.muzika.playlistmanager.dto.PlaylistWithSongsResponse;
import org.muzika.playlistmanager.services.PlaylistService;
import org.muzika.playlistmanager.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
public class PlaylistController {

    private final PlaylistService playlistService;
    private final JwtService jwtService;

    public PlaylistController(PlaylistService playlistService, JwtService jwtService) {
        this.playlistService = playlistService;
        this.jwtService = jwtService;
    }

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String userIdStr = jwtService.extractUserId(token);
            // Try to parse as UUID, if fails assume it's username and we need to handle differently
            try {
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                // If userId is not a UUID, it's likely a username
                // For now, we'll need to handle username->userId mapping
                // This can be enhanced with Kafka communication to AuthorizationManager
                throw new IllegalArgumentException("Unable to extract userId from token. User must be authenticated with a valid token containing userId.");
            }
        }
        throw new IllegalArgumentException("Missing or invalid authorization token");
    }

    @GetMapping("/playlist")
    public ResponseEntity<List<PlaylistResponse>> getAllPlaylists(HttpServletRequest request) {
        UUID userId = getUserIdFromRequest(request);
        List<PlaylistResponse> playlists = playlistService.getAllPlaylists(userId);
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/playlist/{id}")
    public ResponseEntity<PlaylistWithSongsResponse> getPlaylist(@PathVariable UUID id, HttpServletRequest request) {
        UUID userId = getUserIdFromRequest(request);
        PlaylistWithSongsResponse response = playlistService.getPlaylistSongs(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/playlist")
    public ResponseEntity<PlaylistResponse> createPlaylist(@RequestBody CreatePlaylistRequest request, HttpServletRequest httpRequest) {
        UUID userId = getUserIdFromRequest(httpRequest);
        PlaylistResponse response = playlistService.createPlaylist(userId, request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/playlist/{id}/song/{songId}")
    public ResponseEntity<Void> addSongToPlaylist(
            @PathVariable UUID id,
            @PathVariable UUID songId,
            HttpServletRequest request) {
        UUID userId = getUserIdFromRequest(request);
        playlistService.addSongToPlaylist(id, songId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/playlist/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable UUID id, HttpServletRequest request) {
        UUID userId = getUserIdFromRequest(request);
        playlistService.deletePlaylist(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/playlist/{id}/song/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable UUID id,
            @PathVariable UUID songId,
            HttpServletRequest request) {
        UUID userId = getUserIdFromRequest(request);
        playlistService.removeSongFromPlaylist(id, songId, userId);
        return ResponseEntity.noContent().build();
    }
}


