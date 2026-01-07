package org.muzika.playlistmanager.services;

import lombok.extern.slf4j.Slf4j;
import org.muzika.playlistmanager.kafkaMessages.UserCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class KafkaConsumerService {

    private final UserService userService;
    private final PlaylistService playlistService;

    public KafkaConsumerService(UserService userService, PlaylistService playlistService) {
        this.userService = userService;
        this.playlistService = playlistService;
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
}

