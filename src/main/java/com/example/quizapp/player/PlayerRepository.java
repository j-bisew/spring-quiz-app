package com.example.quizapp.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

//    Find players by nickname
    List<Player> findByNickname(String nickname);

//    Find player by session ID
    Optional<Player> findBySessionId(String sessionId);

//    Find active players
    List<Player> findByActiveTrue();

//    Find top players by games played
    @Query("SELECT p FROM Player p WHERE p.active = true ORDER BY p.gamesPlayed DESC")
    List<Player> findTopPlayersByGamesPlayed();

//    Count total players
    long countByActiveTrue();
}