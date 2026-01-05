package org.muzika.playlistmanager.repository;

import org.muzika.playlistmanager.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {
    List<Playlist> findByUserId(UUID userId);
    Optional<Playlist> findByIdAndUserId(UUID id, UUID userId);
}


