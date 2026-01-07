package org.muzika.playlistmanager.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    private String userName;

    private UUID userId; // from auth

    public User(UUID uuid, String username) {
        this.userId = uuid;
        this.userName = username;
    }
}

