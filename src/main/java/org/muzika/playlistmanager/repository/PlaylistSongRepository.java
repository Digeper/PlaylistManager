package org.muzika.playlistmanager.repository;

import org.muzika.playlistmanager.entities.PlaylistSong;
import org.muzika.playlistmanager.entities.PlaylistSongId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSongId> {
    List<PlaylistSong> findByPlaylistIdOrderByPositionAsc(UUID playlistId);
    void deleteByPlaylistIdAndSongId(UUID playlistId, UUID songId);
    void deleteByPlaylistId(UUID playlistId);
}


