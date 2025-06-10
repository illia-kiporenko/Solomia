package bots.bot.coin;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT p FROM Profile p WHERE idDiscord = ?1")
    Optional<Profile> findByDiscordId(long idDiscord);

    @Query("SELECT p FROM Profile p JOIN p.playlists pl WHERE pl.name = :playlistName")
    Optional<Profile> findProfileByPlaylistName(String playlistName);
}

