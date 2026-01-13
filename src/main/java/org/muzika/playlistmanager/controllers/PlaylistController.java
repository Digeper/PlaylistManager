package org.muzika.playlistmanager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Playlist", description = "Playlist management endpoints")
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
    @Operation(
        summary = "Get all playlists",
        description = "Retrieve all playlists owned by the authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Playlists retrieved successfully",
            content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (invalid or missing JWT token)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<List<PlaylistResponse>> getAllPlaylists() {
        String username = getAuthenticatedUsername();
        List<PlaylistResponse> playlists = playlistService.getAllPlaylists(username);
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get playlist with songs",
        description = "Get a specific playlist by ID with all its songs. Only accessible by the playlist owner."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Playlist retrieved successfully",
            content = @Content(schema = @Schema(implementation = PlaylistWithSongsResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (invalid or missing JWT token)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Playlist not found or access denied"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<PlaylistWithSongsResponse> getPlaylist(
        @Parameter(description = "Playlist UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id) {
        String username = getAuthenticatedUsername();
        PlaylistWithSongsResponse response = playlistService.getPlaylistSongs(id, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
        summary = "Create new playlist",
        description = "Create a new playlist for the authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Playlist created successfully",
            content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation error, missing required fields)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (invalid or missing JWT token)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<PlaylistResponse> createPlaylist(
        @Parameter(description = "Playlist creation data", required = true)
        @RequestBody CreatePlaylistRequest request) {
        String username = getAuthenticatedUsername();
        PlaylistResponse response = playlistService.createPlaylist(username, request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/song/{songId}")
    @Operation(
        summary = "Add song to playlist",
        description = "Add a song (by UUID) to a playlist. Only accessible by the playlist owner."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Song added to playlist successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (song already in playlist, validation error)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (invalid or missing JWT token)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Playlist not found or access denied"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<Void> addSongToPlaylist(
        @Parameter(description = "Playlist UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id,
        @Parameter(description = "Song UUID (reference to QueueManager Song)", required = true, example = "770e8400-e29b-41d4-a716-446655440002")
        @PathVariable UUID songId) {
        String username = getAuthenticatedUsername();
        playlistService.addSongToPlaylist(id, songId, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete playlist",
        description = "Delete a playlist by ID. Only accessible by the playlist owner."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Playlist deleted successfully (No Content)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (invalid or missing JWT token)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Playlist not found or access denied"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<Void> deletePlaylist(
        @Parameter(description = "Playlist UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id) {
        String username = getAuthenticatedUsername();
        playlistService.deletePlaylist(id, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/song/{songId}")
    @Operation(
        summary = "Remove song from playlist",
        description = "Remove a song from a playlist. Only accessible by the playlist owner."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Song removed from playlist successfully (No Content)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (invalid or missing JWT token)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Playlist or song not found, or access denied"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<Void> removeSongFromPlaylist(
        @Parameter(description = "Playlist UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id,
        @Parameter(description = "Song UUID", required = true, example = "770e8400-e29b-41d4-a716-446655440002")
        @PathVariable UUID songId) {
        String username = getAuthenticatedUsername();
        playlistService.removeSongFromPlaylist(id, songId, username);
        return ResponseEntity.noContent().build();
    }
}


