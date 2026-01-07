package org.muzika.playlistmanager.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.muzika.playlistmanager.dto.PlaylistResponse;
import org.muzika.playlistmanager.dto.PlaylistWithSongsResponse;
import org.muzika.playlistmanager.entities.Playlist;
import org.muzika.playlistmanager.entities.PlaylistSong;
import org.muzika.playlistmanager.entities.PlaylistSongId;
import org.muzika.playlistmanager.entities.User;
import org.muzika.playlistmanager.repository.PlaylistRepository;
import org.muzika.playlistmanager.repository.PlaylistSongRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistSongRepository playlistSongRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PlaylistService playlistService;

    private User testUser;
    private UUID userId;
    private UUID playlistId;
    private UUID songId;
    private UUID songId2;
    private String username;
    private Playlist testPlaylist;
    private PlaylistSong testPlaylistSong;

    @BeforeEach
    void setUp() {
        username = "testuser";
        userId = UUID.randomUUID();
        playlistId = UUID.randomUUID();
        songId = UUID.randomUUID();
        songId2 = UUID.randomUUID();

        testUser = new User();
        testUser.setUuid(userId);
        testUser.setUserName(username);

        testPlaylist = new Playlist();
        testPlaylist.setId(playlistId);
        testPlaylist.setUserId(userId);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setDescription("Test Description");

        testPlaylistSong = new PlaylistSong();
        testPlaylistSong.setPlaylistId(playlistId);
        testPlaylistSong.setSongId(songId);
        testPlaylistSong.setPosition(0);
    }

    @Test
    void testGetAllPlaylists_Success() {
        // Arrange
        List<Playlist> playlists = Arrays.asList(testPlaylist);
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByUserId(userId)).thenReturn(playlists);

        // Act
        List<PlaylistResponse> result = playlistService.getAllPlaylists(username);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(playlistId, result.get(0).getId());
        assertEquals("Test Playlist", result.get(0).getName());
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetAllPlaylists_EmptyList() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

        // Act
        List<PlaylistResponse> result = playlistService.getAllPlaylists(username);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetAllPlaylists_UserNotFound() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenThrow(new IllegalArgumentException("User not found: " + username));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.getAllPlaylists(username);
        });

        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, never()).findByUserId(any());
    }

    @Test
    void testGetPlaylistById_Success() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));

        // Act
        PlaylistResponse result = playlistService.getPlaylistById(playlistId, username);

        // Assert
        assertNotNull(result);
        assertEquals(playlistId, result.getId());
        assertEquals("Test Playlist", result.getName());
        assertEquals("Test Description", result.getDescription());
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
    }

    @Test
    void testGetPlaylistById_NotFound() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.getPlaylistById(playlistId, username);
        }, "Playlist not found or access denied");

        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
    }

    @Test
    void testGetPlaylistById_AccessDenied() {
        // Arrange - Different user
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User();
        otherUser.setUuid(otherUserId);
        otherUser.setUserName("otheruser");

        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.getPlaylistById(playlistId, username);
        }, "Playlist not found or access denied");
    }

    @Test
    void testGetPlaylistSongs_Success() {
        // Arrange
        List<PlaylistSong> playlistSongs = Arrays.asList(testPlaylistSong);
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId)).thenReturn(playlistSongs);

        // Act
        PlaylistWithSongsResponse result = playlistService.getPlaylistSongs(playlistId, username);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPlaylist());
        assertNotNull(result.getSongs());
        assertEquals(1, result.getSongs().size());
        assertEquals(playlistId, result.getPlaylist().getId());
        assertEquals(songId, result.getSongs().get(0).getSongId());
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistSongRepository, times(1)).findByPlaylistIdOrderByPositionAsc(playlistId);
    }

    @Test
    void testGetPlaylistSongs_EmptySongs() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId)).thenReturn(new ArrayList<>());

        // Act
        PlaylistWithSongsResponse result = playlistService.getPlaylistSongs(playlistId, username);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPlaylist());
        assertNotNull(result.getSongs());
        assertTrue(result.getSongs().isEmpty());
        verify(playlistSongRepository, times(1)).findByPlaylistIdOrderByPositionAsc(playlistId);
    }

    @Test
    void testGetPlaylistSongs_NotFound() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.getPlaylistSongs(playlistId, username);
        }, "Playlist not found or access denied");

        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistSongRepository, never()).findByPlaylistIdOrderByPositionAsc(any());
    }

    @Test
    void testCreatePlaylist_Success() {
        // Arrange
        String name = "New Playlist";
        String description = "New Description";
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist p = invocation.getArgument(0);
            p.setId(playlistId);
            return p;
        });

        // Act
        PlaylistResponse result = playlistService.createPlaylist(username, name, description);

        // Assert
        assertNotNull(result);
        assertEquals(playlistId, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    void testCreatePlaylist_WithoutDescription() {
        // Arrange
        String name = "New Playlist";
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist p = invocation.getArgument(0);
            p.setId(playlistId);
            return p;
        });

        // Act
        PlaylistResponse result = playlistService.createPlaylist(username, name, null);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertNull(result.getDescription());
        verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    void testCreatePlaylist_NullName() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.createPlaylist(username, null, "Description");
        }, "Playlist name is required");

        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    void testCreatePlaylist_EmptyName() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.createPlaylist(username, "", "Description");
        }, "Playlist name is required");

        verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    void testCreatePlaylist_WhitespaceName() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.createPlaylist(username, "   ", "Description");
        }, "Playlist name is required");

        verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    void testCreatePlaylist_TrimsNameAndDescription() {
        // Arrange
        String name = "  New Playlist  ";
        String description = "  New Description  ";
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist p = invocation.getArgument(0);
            p.setId(playlistId);
            return p;
        });

        // Act
        PlaylistResponse result = playlistService.createPlaylist(username, name, description);

        // Assert
        assertEquals("New Playlist", result.getName());
        assertEquals("New Description", result.getDescription());
    }

    @Test
    void testAddSongToPlaylist_Success() {
        // Arrange
        PlaylistSongId playlistSongId = new PlaylistSongId();
        playlistSongId.setPlaylistId(playlistId);
        playlistSongId.setSongId(songId);

        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));
        when(playlistSongRepository.existsById(playlistSongId)).thenReturn(false);
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId)).thenReturn(new ArrayList<>());
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        playlistService.addSongToPlaylist(playlistId, songId, username);

        // Assert
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistSongRepository, times(1)).existsById(playlistSongId);
        verify(playlistSongRepository, times(1)).findByPlaylistIdOrderByPositionAsc(playlistId);
        verify(playlistSongRepository, times(1)).save(any(PlaylistSong.class));
    }

    @Test
    void testAddSongToPlaylist_WithExistingSongs_NextPosition() {
        // Arrange
        PlaylistSong existingSong = new PlaylistSong();
        existingSong.setPlaylistId(playlistId);
        existingSong.setSongId(songId);
        existingSong.setPosition(0);

        PlaylistSongId playlistSongId = new PlaylistSongId();
        playlistSongId.setPlaylistId(playlistId);
        playlistSongId.setSongId(songId2);

        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));
        when(playlistSongRepository.existsById(playlistSongId)).thenReturn(false);
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId)).thenReturn(Arrays.asList(existingSong));
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        playlistService.addSongToPlaylist(playlistId, songId2, username);

        // Assert
        verify(playlistSongRepository, times(1)).save(argThat(ps -> ps.getPosition() == 1));
    }

    @Test
    void testAddSongToPlaylist_DuplicateSong() {
        // Arrange
        PlaylistSongId playlistSongId = new PlaylistSongId();
        playlistSongId.setPlaylistId(playlistId);
        playlistSongId.setSongId(songId);

        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));
        when(playlistSongRepository.existsById(playlistSongId)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.addSongToPlaylist(playlistId, songId, username);
        }, "Song already exists in playlist");

        verify(playlistSongRepository, times(1)).existsById(playlistSongId);
        verify(playlistSongRepository, never()).save(any(PlaylistSong.class));
    }

    @Test
    void testAddSongToPlaylist_PlaylistNotFound() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.addSongToPlaylist(playlistId, songId, username);
        }, "Playlist not found or access denied");

        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistSongRepository, never()).existsById(any());
    }

    @Test
    void testRemoveSongFromPlaylist_Success() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));
        doNothing().when(playlistSongRepository).deleteByPlaylistIdAndSongId(playlistId, songId);

        // Act
        playlistService.removeSongFromPlaylist(playlistId, songId, username);

        // Assert
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistSongRepository, times(1)).deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    @Test
    void testRemoveSongFromPlaylist_PlaylistNotFound() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.removeSongFromPlaylist(playlistId, songId, username);
        }, "Playlist not found or access denied");

        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistSongRepository, never()).deleteByPlaylistIdAndSongId(any(), any());
    }

    @Test
    void testDeletePlaylist_Success() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(testPlaylist));
        doNothing().when(playlistRepository).delete(testPlaylist);

        // Act
        playlistService.deletePlaylist(playlistId, username);

        // Assert
        verify(userService, times(1)).getUserOrThrow(username);
        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistRepository, times(1)).delete(testPlaylist);
    }

    @Test
    void testDeletePlaylist_NotFound() {
        // Arrange
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.deletePlaylist(playlistId, username);
        }, "Playlist not found or access denied");

        verify(playlistRepository, times(1)).findByIdAndUserId(playlistId, userId);
        verify(playlistRepository, never()).delete(any(Playlist.class));
    }

    @Test
    void testDeletePlaylist_AccessDenied() {
        // Arrange - Different user
        UUID otherUserId = UUID.randomUUID();
        when(userService.getUserOrThrow(username)).thenReturn(testUser);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            playlistService.deletePlaylist(playlistId, username);
        }, "Playlist not found or access denied");
    }
}

