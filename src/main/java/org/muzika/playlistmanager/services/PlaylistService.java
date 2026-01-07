package org.muzika.playlistmanager.services;

import org.muzika.playlistmanager.dto.PlaylistResponse;
import org.muzika.playlistmanager.dto.PlaylistSongResponse;
import org.muzika.playlistmanager.dto.PlaylistWithSongsResponse;
import org.muzika.playlistmanager.entities.Playlist;
import org.muzika.playlistmanager.entities.PlaylistSong;
import org.muzika.playlistmanager.entities.PlaylistSongId;
import org.muzika.playlistmanager.entities.User;
import org.muzika.playlistmanager.repository.PlaylistRepository;
import org.muzika.playlistmanager.repository.PlaylistSongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final UserService userService;

    public PlaylistService(PlaylistRepository playlistRepository, PlaylistSongRepository playlistSongRepository, UserService userService) {
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.userService = userService;
    }

    public List<PlaylistResponse> getAllPlaylists(String username) {
        User user = userService.getUserOrThrow(username);
        // Use userId (auth userId) not uuid (auto-generated PK) for consistency
        UUID userId = user.getUserId() != null ? user.getUserId() : user.getUuid();
        List<Playlist> playlists = playlistRepository.findByUserId(userId);
        return playlists.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public PlaylistResponse getPlaylistById(UUID playlistId, String username) {
        User user = userService.getUserOrThrow(username);
        // Use userId (auth userId) not uuid (auto-generated PK) for consistency
        UUID userId = user.getUserId() != null ? user.getUserId() : user.getUuid();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found or access denied"));
        return convertToResponse(playlist);
    }

    public PlaylistWithSongsResponse getPlaylistSongs(UUID playlistId, String username) {
        User user = userService.getUserOrThrow(username);
        // Use userId (auth userId) not uuid (auto-generated PK) for consistency
        UUID userId = user.getUserId() != null ? user.getUserId() : user.getUuid();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found or access denied"));
        
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);
        
        PlaylistWithSongsResponse response = new PlaylistWithSongsResponse();
        response.setPlaylist(convertToResponse(playlist));
        response.setSongs(playlistSongs.stream()
                .map(this::convertToSongResponse)
                .collect(Collectors.toList()));
        
        return response;
    }

    public PlaylistResponse createPlaylist(String username, String name, String description) {
        User user = userService.getUserOrThrow(username);
        // Use userId (auth userId) not uuid (auto-generated PK) for consistency across services
        UUID userId = user.getUserId() != null ? user.getUserId() : user.getUuid();
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Playlist name is required");
        }

        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setName(name.trim());
        playlist.setDescription(description != null ? description.trim() : null);

        playlist = playlistRepository.save(playlist);
        
        return convertToResponse(playlist);
    }

    public void addSongToPlaylist(UUID playlistId, UUID songId, String username) {
        User user = userService.getUserOrThrow(username);
        // Use userId (auth userId) not uuid (auto-generated PK) for consistency
        UUID userId = user.getUserId() != null ? user.getUserId() : user.getUuid();
        
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found or access denied"));

        // Check if song already exists in playlist
        PlaylistSongId id = new PlaylistSongId();
        id.setPlaylistId(playlistId);
        id.setSongId(songId);
        
        if (playlistSongRepository.existsById(id)) {
            throw new IllegalArgumentException("Song already exists in playlist");
        }

        // Get the next position (highest position + 1)
        List<PlaylistSong> existingSongs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);
        int nextPosition = existingSongs.isEmpty() ? 0 : existingSongs.get(existingSongs.size() - 1).getPosition() + 1;

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylistId(playlistId);
        playlistSong.setSongId(songId);
        playlistSong.setPosition(nextPosition);
        playlistSong.setPlaylist(playlist);

        playlistSongRepository.save(playlistSong);
    }

    public void removeSongFromPlaylist(UUID playlistId, UUID songId, String username) {
        User user = userService.getUserOrThrow(username);
        // Use userId (auth userId) not uuid (auto-generated PK) for consistency
        UUID userId = user.getUserId() != null ? user.getUserId() : user.getUuid();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found or access denied"));

        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    public void deletePlaylist(UUID playlistId, String username) {
        User user = userService.getUserOrThrow(username);
        // Use userId (auth userId) not uuid (auto-generated PK) for consistency
        UUID userId = user.getUserId() != null ? user.getUserId() : user.getUuid();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found or access denied"));

        playlistRepository.delete(playlist);
    }

    private PlaylistResponse convertToResponse(Playlist playlist) {
        PlaylistResponse response = new PlaylistResponse();
        response.setId(playlist.getId());
        response.setUserId(playlist.getUserId());
        response.setName(playlist.getName());
        response.setDescription(playlist.getDescription());
        response.setCreatedAt(playlist.getCreatedAt());
        response.setUpdatedAt(playlist.getUpdatedAt());
        return response;
    }

    private PlaylistSongResponse convertToSongResponse(PlaylistSong playlistSong) {
        PlaylistSongResponse response = new PlaylistSongResponse();
        response.setSongId(playlistSong.getSongId());
        response.setPosition(playlistSong.getPosition());
        response.setAddedAt(playlistSong.getAddedAt());
        return response;
    }
}

