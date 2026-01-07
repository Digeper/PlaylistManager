package org.muzika.playlistmanager.services;

import lombok.extern.slf4j.Slf4j;
import org.muzika.playlistmanager.kafkaMessages.LikedSongEvent;
import org.muzika.playlistmanager.kafkaMessages.UnlikedSongEvent;
import org.muzika.playlistmanager.kafkaMessages.UserCreatedEvent;
import org.muzika.playlistmanager.repository.PlaylistRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
public class KafkaConsumerService {

    private final UserService userService;
    private final PlaylistService playlistService;
    private final PlaylistRepository playlistRepository;

    public KafkaConsumerService(UserService userService, PlaylistService playlistService, PlaylistRepository playlistRepository) {
        this.userService = userService;
        this.playlistService = playlistService;
        this.playlistRepository = playlistRepository;
    }

    @KafkaListener(topics = {"user-created"}, groupId = "playlist-manager-group", containerFactory = "userCreatedListenerContainerFactory")
    @Transactional
    public void consumeUserCreatedEvent(UserCreatedEvent event) {
        log.info("Received user created event: userId={}, username={}", event.getUserId(), event.getUsername());
        
        try {
            // Create or get user in PlaylistManager database
            userService.getOrCreateUser(event.getUsername(), event.getUserId());
            log.info("Successfully created/retrieved user in PlaylistManager: userId={}, username={}", 
                    event.getUserId(), event.getUsername());
            
            // Create "Liked" playlist for the new user
            playlistService.createPlaylist(event.getUsername(), "Liked", null);
            log.info("Successfully created 'Liked' playlist for user: userId={}, username={}", 
                    event.getUserId(), event.getUsername());
            
        } catch (Exception e) {
            log.error("Failed to process user created event: userId={}, username={}, error={}",
                    event.getUserId(), event.getUsername(), e.getMessage(), e);
            // Re-throw to allow Kafka to handle retry if configured
            throw new RuntimeException("Failed to process user created event", e);
        }
    }

    @KafkaListener(topics = {"liked"}, groupId = "playlist-manager-group", containerFactory = "likedSongListenerContainerFactory")
    @Transactional
    public void consumeLikedSongEvent(LikedSongEvent event) {
        log.info("Received liked song event: userId={}, username={}, songId={}", 
                event.getUserId(), event.getUsername(), event.getSongId());
        
        try {
            // Find user's "Liked" playlist
            var likedPlaylistOpt = playlistRepository.findByUserIdAndName(event.getUserId(), "Liked");
            
            if (likedPlaylistOpt.isEmpty()) {
                log.error("Liked playlist not found for user: userId={}, username={}. Creating it now.", 
                        event.getUserId(), event.getUsername());
                
                // Create the "Liked" playlist if it doesn't exist
                playlistService.createPlaylist(event.getUsername(), "Liked", null);
                
                likedPlaylistOpt = playlistRepository.findByUserIdAndName(event.getUserId(), "Liked");
                
                if (likedPlaylistOpt.isEmpty()) {
                    log.error("Failed to create or find Liked playlist for user: userId={}, username={}", 
                            event.getUserId(), event.getUsername());
                    return;
                }
            }
            
            UUID playlistId = likedPlaylistOpt.get().getId();
            
            // Add song to the "Liked" playlist
            playlistService.addSongToPlaylist(playlistId, event.getSongId(), event.getUsername());
            
            log.info("Successfully added song to Liked playlist: userId={}, username={}, songId={}, playlistId={}", 
                    event.getUserId(), event.getUsername(), event.getSongId(), playlistId);
            
        } catch (IllegalArgumentException e) {
            // Song already exists in playlist - this is fine, just log it
            log.debug("Song already exists in Liked playlist: userId={}, songId={}, message={}", 
                    event.getUserId(), event.getSongId(), e.getMessage());
        } catch (Exception e) {
            log.error("Failed to process liked song event: userId={}, username={}, songId={}, error={}",
                    event.getUserId(), event.getUsername(), event.getSongId(), e.getMessage(), e);
            // Don't re-throw to avoid infinite retries for non-recoverable errors
        }
    }

    @KafkaListener(topics = {"unliked"}, groupId = "playlist-manager-group", containerFactory = "unlikedSongListenerContainerFactory")
    @Transactional
    public void consumeUnlikedSongEvent(UnlikedSongEvent event) {
        log.info("Received unliked song event: userId={}, username={}, songId={}", 
                event.getUserId(), event.getUsername(), event.getSongId());
        
        try {
            // Find user's "Liked" playlist
            var likedPlaylistOpt = playlistRepository.findByUserIdAndName(event.getUserId(), "Liked");
            
            if (likedPlaylistOpt.isEmpty()) {
                log.warn("Liked playlist not found for user: userId={}, username={}. Cannot remove song.", 
                        event.getUserId(), event.getUsername());
                return;
            }
            
            UUID playlistId = likedPlaylistOpt.get().getId();
            
            // Remove song from the "Liked" playlist
            playlistService.removeSongFromPlaylist(playlistId, event.getSongId(), event.getUsername());
            log.info("Successfully removed song from Liked playlist: userId={}, username={}, songId={}, playlistId={}", 
                    event.getUserId(), event.getUsername(), event.getSongId(), playlistId);
            
        } catch (Exception e) {
            log.error("Failed to process unliked song event: userId={}, username={}, songId={}, error={}",
                    event.getUserId(), event.getUsername(), event.getSongId(), e.getMessage(), e);
            // Don't re-throw to avoid infinite retries for non-recoverable errors
        }
    }
}

