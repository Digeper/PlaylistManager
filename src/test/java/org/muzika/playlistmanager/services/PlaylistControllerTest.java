package org.muzika.playlistmanager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.muzika.playlistmanager.controllers.PlaylistController;
import org.muzika.playlistmanager.dto.CreatePlaylistRequest;
import org.muzika.playlistmanager.dto.PlaylistResponse;
import org.muzika.playlistmanager.dto.PlaylistSongResponse;
import org.muzika.playlistmanager.dto.PlaylistWithSongsResponse;
import org.muzika.playlistmanager.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlaylistController.class, excludeAutoConfiguration = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        CacheAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.data.jpa.repositories.enabled=false"
})
@AutoConfigureMockMvc(addFilters = false)
class PlaylistControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public EntityManagerFactory entityManagerFactory() {
            return org.mockito.Mockito.mock(EntityManagerFactory.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID playlistId;
    private UUID songId;
    private UUID userId;
    private String username;
    private PlaylistResponse testPlaylistResponse;
    private PlaylistWithSongsResponse testPlaylistWithSongsResponse;

    @BeforeEach
    void setUp() {
        username = "testuser";
        userId = UUID.randomUUID();
        playlistId = UUID.randomUUID();
        songId = UUID.randomUUID();

        testPlaylistResponse = new PlaylistResponse();
        testPlaylistResponse.setId(playlistId);
        testPlaylistResponse.setUserId(userId);
        testPlaylistResponse.setName("Test Playlist");
        testPlaylistResponse.setDescription("Test Description");
        testPlaylistResponse.setCreatedAt(LocalDateTime.now());
        testPlaylistResponse.setUpdatedAt(LocalDateTime.now());

        PlaylistSongResponse songResponse = new PlaylistSongResponse();
        songResponse.setSongId(songId);
        songResponse.setPosition(0);
        songResponse.setAddedAt(LocalDateTime.now());

        testPlaylistWithSongsResponse = new PlaylistWithSongsResponse();
        testPlaylistWithSongsResponse.setPlaylist(testPlaylistResponse);
        testPlaylistWithSongsResponse.setSongs(Arrays.asList(songResponse));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPlaylists_Success() throws Exception {
        // Arrange
        List<PlaylistResponse> playlists = Arrays.asList(testPlaylistResponse);
        when(playlistService.getAllPlaylists(username)).thenReturn(playlists);

        // Act & Assert
        mockMvc.perform(get("/playlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(playlistId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Playlist"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));

        verify(playlistService, times(1)).getAllPlaylists(username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPlaylists_EmptyList() throws Exception {
        // Arrange
        when(playlistService.getAllPlaylists(username)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/playlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(playlistService, times(1)).getAllPlaylists(username);
    }

    @Test
    void testGetAllPlaylists_Unauthorized() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        mockMvc.perform(get("/playlist"))
                .andExpect(status().isUnauthorized());

        verify(playlistService, never()).getAllPlaylists(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPlaylists_InternalServerError() throws Exception {
        // Arrange
        when(playlistService.getAllPlaylists(username)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/playlist"))
                .andExpect(status().isInternalServerError());

        verify(playlistService, times(1)).getAllPlaylists(username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetPlaylist_Success() throws Exception {
        // Arrange
        when(playlistService.getPlaylistSongs(playlistId, username)).thenReturn(testPlaylistWithSongsResponse);

        // Act & Assert
        mockMvc.perform(get("/playlist/{id}", playlistId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playlist.id").value(playlistId.toString()))
                .andExpect(jsonPath("$.playlist.name").value("Test Playlist"))
                .andExpect(jsonPath("$.songs").isArray())
                .andExpect(jsonPath("$.songs[0].songId").value(songId.toString()));

        verify(playlistService, times(1)).getPlaylistSongs(playlistId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetPlaylist_NotFound() throws Exception {
        // Arrange
        when(playlistService.getPlaylistSongs(playlistId, username))
                .thenThrow(new IllegalArgumentException("Playlist not found or access denied"));

        // Act & Assert
        mockMvc.perform(get("/playlist/{id}", playlistId))
                .andExpect(status().isBadRequest());

        verify(playlistService, times(1)).getPlaylistSongs(playlistId, username);
    }

    @Test
    void testGetPlaylist_Unauthorized() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        mockMvc.perform(get("/playlist/{id}", playlistId))
                .andExpect(status().isUnauthorized());

        verify(playlistService, never()).getPlaylistSongs(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetPlaylist_EmptySongs() throws Exception {
        // Arrange
        PlaylistWithSongsResponse emptyResponse = new PlaylistWithSongsResponse();
        emptyResponse.setPlaylist(testPlaylistResponse);
        emptyResponse.setSongs(new ArrayList<>());

        when(playlistService.getPlaylistSongs(playlistId, username)).thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/playlist/{id}", playlistId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs").isArray())
                .andExpect(jsonPath("$.songs").isEmpty());

        verify(playlistService, times(1)).getPlaylistSongs(playlistId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePlaylist_Success() throws Exception {
        // Arrange
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");
        request.setDescription("New Description");

        PlaylistResponse newPlaylist = new PlaylistResponse();
        newPlaylist.setId(playlistId);
        newPlaylist.setName("New Playlist");
        newPlaylist.setDescription("New Description");

        when(playlistService.createPlaylist(eq(username), eq("New Playlist"), eq("New Description")))
                .thenReturn(newPlaylist);

        // Act & Assert
        mockMvc.perform(post("/playlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(playlistId.toString()))
                .andExpect(jsonPath("$.name").value("New Playlist"))
                .andExpect(jsonPath("$.description").value("New Description"));

        verify(playlistService, times(1)).createPlaylist(username, "New Playlist", "New Description");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePlaylist_WithoutDescription() throws Exception {
        // Arrange
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");
        request.setDescription(null);

        PlaylistResponse newPlaylist = new PlaylistResponse();
        newPlaylist.setId(playlistId);
        newPlaylist.setName("New Playlist");
        newPlaylist.setDescription(null);

        when(playlistService.createPlaylist(eq(username), eq("New Playlist"), eq(null)))
                .thenReturn(newPlaylist);

        // Act & Assert
        mockMvc.perform(post("/playlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Playlist"));

        verify(playlistService, times(1)).createPlaylist(username, "New Playlist", null);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePlaylist_InvalidRequest_NullName() throws Exception {
        // Arrange
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName(null);
        request.setDescription("Description");

        when(playlistService.createPlaylist(eq(username), eq(null), eq("Description")))
                .thenThrow(new IllegalArgumentException("Playlist name is required"));

        // Act & Assert
        mockMvc.perform(post("/playlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(playlistService, times(1)).createPlaylist(username, null, "Description");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePlaylist_InvalidRequest_EmptyName() throws Exception {
        // Arrange
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("");
        request.setDescription("Description");

        when(playlistService.createPlaylist(eq(username), eq(""), eq("Description")))
                .thenThrow(new IllegalArgumentException("Playlist name is required"));

        // Act & Assert
        mockMvc.perform(post("/playlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(playlistService, times(1)).createPlaylist(username, "", "Description");
    }

    @Test
    void testCreatePlaylist_Unauthorized() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");

        // Act & Assert
        mockMvc.perform(post("/playlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(playlistService, never()).createPlaylist(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePlaylist_InternalServerError() throws Exception {
        // Arrange
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");

        when(playlistService.createPlaylist(eq(username), eq("New Playlist"), eq(null)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/playlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(playlistService, times(1)).createPlaylist(username, "New Playlist", null);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddSongToPlaylist_Success() throws Exception {
        // Arrange
        doNothing().when(playlistService).addSongToPlaylist(playlistId, songId, username);

        // Act & Assert
        mockMvc.perform(post("/playlist/{id}/song/{songId}", playlistId, songId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(playlistService, times(1)).addSongToPlaylist(playlistId, songId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddSongToPlaylist_DuplicateSong() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Song already exists in playlist"))
                .when(playlistService).addSongToPlaylist(playlistId, songId, username);

        // Act & Assert
        mockMvc.perform(post("/playlist/{id}/song/{songId}", playlistId, songId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(playlistService, times(1)).addSongToPlaylist(playlistId, songId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddSongToPlaylist_PlaylistNotFound() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Playlist not found or access denied"))
                .when(playlistService).addSongToPlaylist(playlistId, songId, username);

        // Act & Assert
        mockMvc.perform(post("/playlist/{id}/song/{songId}", playlistId, songId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(playlistService, times(1)).addSongToPlaylist(playlistId, songId, username);
    }

    @Test
    void testAddSongToPlaylist_Unauthorized() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        mockMvc.perform(post("/playlist/{id}/song/{songId}", playlistId, songId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(playlistService, never()).addSongToPlaylist(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddSongToPlaylist_InternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database error"))
                .when(playlistService).addSongToPlaylist(playlistId, songId, username);

        // Act & Assert
        mockMvc.perform(post("/playlist/{id}/song/{songId}", playlistId, songId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(playlistService, times(1)).addSongToPlaylist(playlistId, songId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeletePlaylist_Success() throws Exception {
        // Arrange
        doNothing().when(playlistService).deletePlaylist(playlistId, username);

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}", playlistId))
                .andExpect(status().isNoContent());

        verify(playlistService, times(1)).deletePlaylist(playlistId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeletePlaylist_NotFound() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Playlist not found or access denied"))
                .when(playlistService).deletePlaylist(playlistId, username);

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}", playlistId))
                .andExpect(status().isBadRequest());

        verify(playlistService, times(1)).deletePlaylist(playlistId, username);
    }

    @Test
    void testDeletePlaylist_Unauthorized() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}", playlistId))
                .andExpect(status().isUnauthorized());

        verify(playlistService, never()).deletePlaylist(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeletePlaylist_InternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database error"))
                .when(playlistService).deletePlaylist(playlistId, username);

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}", playlistId))
                .andExpect(status().isInternalServerError());

        verify(playlistService, times(1)).deletePlaylist(playlistId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRemoveSongFromPlaylist_Success() throws Exception {
        // Arrange
        doNothing().when(playlistService).removeSongFromPlaylist(playlistId, songId, username);

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}/song/{songId}", playlistId, songId))
                .andExpect(status().isNoContent());

        verify(playlistService, times(1)).removeSongFromPlaylist(playlistId, songId, username);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRemoveSongFromPlaylist_PlaylistNotFound() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Playlist not found or access denied"))
                .when(playlistService).removeSongFromPlaylist(playlistId, songId, username);

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}/song/{songId}", playlistId, songId))
                .andExpect(status().isBadRequest());

        verify(playlistService, times(1)).removeSongFromPlaylist(playlistId, songId, username);
    }

    @Test
    void testRemoveSongFromPlaylist_Unauthorized() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}/song/{songId}", playlistId, songId))
                .andExpect(status().isUnauthorized());

        verify(playlistService, never()).removeSongFromPlaylist(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRemoveSongFromPlaylist_InternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database error"))
                .when(playlistService).removeSongFromPlaylist(playlistId, songId, username);

        // Act & Assert
        mockMvc.perform(delete("/playlist/{id}/song/{songId}", playlistId, songId))
                .andExpect(status().isInternalServerError());

        verify(playlistService, times(1)).removeSongFromPlaylist(playlistId, songId, username);
    }
}

