package org.muzika.playlistmanager.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.muzika.playlistmanager.entities.User;
import org.muzika.playlistmanager.repository.UserRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;
    private String username;

    @BeforeEach
    void setUp() {
        username = "testuser";
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setUuid(userId);
        testUser.setUserName(username);
    }

    @Test
    void testGetUserByName_Found() {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(testUser);

        // Act
        User result = userService.getUserByName(username);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUuid());
        assertEquals(username, result.getUserName());
        verify(userRepository, times(1)).findByUserName(username);
    }

    @Test
    void testGetUserByName_NotFound() {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(null);

        // Act
        User result = userService.getUserByName(username);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findByUserName(username);
    }

    @Test
    void testGetUserOrThrow_Found() {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(testUser);

        // Act
        User result = userService.getUserOrThrow(username);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUuid());
        assertEquals(username, result.getUserName());
        verify(userRepository, times(1)).findByUserName(username);
    }

    @Test
    void testGetUserOrThrow_NotFound() {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserOrThrow(username);
        });

        assertEquals("User not found: " + username, exception.getMessage());
        verify(userRepository, times(1)).findByUserName(username);
    }

    @Test
    void testGetUserIdByUsername_Success() {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(testUser);

        // Act
        UUID result = userService.getUserIdByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result);
        verify(userRepository, times(1)).findByUserName(username);
    }

    @Test
    void testGetUserIdByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserIdByUsername(username);
        });

        assertEquals("User not found: " + username, exception.getMessage());
        verify(userRepository, times(1)).findByUserName(username);
    }

    @Test
    void testGetOrCreateUser_ExistingUser() {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(testUser);

        // Act
        User result = userService.getOrCreateUser(username, userId);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
        assertEquals(userId, result.getUuid());
        assertEquals(username, result.getUserName());
        verify(userRepository, times(1)).findByUserName(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetOrCreateUser_NewUser() {
        // Arrange
        UUID newUserId = UUID.randomUUID();
        when(userRepository.findByUserName(username)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // Act
        User result = userService.getOrCreateUser(username, newUserId);

        // Assert
        assertNotNull(result);
        assertEquals(newUserId, result.getUuid());
        assertEquals(username, result.getUserName());
        verify(userRepository, times(1)).findByUserName(username);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetOrCreateUser_NewUser_CreatesWithCorrectParameters() {
        // Arrange
        UUID newUserId = UUID.randomUUID();
        when(userRepository.findByUserName(username)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.getOrCreateUser(username, newUserId);

        // Assert
        verify(userRepository, times(1)).save(argThat(user ->
                user.getUuid().equals(newUserId) && user.getUserName().equals(username)
        ));
    }

    @Test
    void testGetUserByName_NullUsername() {
        // Arrange
        when(userRepository.findByUserName(null)).thenReturn(null);

        // Act
        User result = userService.getUserByName(null);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findByUserName(null);
    }

    @Test
    void testGetUserOrThrow_NullUsername() {
        // Arrange
        when(userRepository.findByUserName(null)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserOrThrow(null);
        });

        assertEquals("User not found: null", exception.getMessage());
    }
}

