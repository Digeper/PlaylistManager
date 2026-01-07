package org.muzika.playlistmanager.services;

import jakarta.transaction.Transactional;
import org.muzika.playlistmanager.entities.User;
import org.muzika.playlistmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByName(String username) {
        return userRepository.findByUserName(username);
    }

    public User getUserOrThrow(String username) {
        User user = getUserByName(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        return user;
    }

    public UUID getUserIdByUsername(String username) {
        User user = getUserOrThrow(username);
        return user.getUuid();
    }

    public User getOrCreateUser(String username, UUID userId) {
        User existingUser = getUserByName(username);
        if (existingUser != null) {
            return existingUser;
        }

        User newUser = new User(userId, username);
        return userRepository.save(newUser);
    }
}

