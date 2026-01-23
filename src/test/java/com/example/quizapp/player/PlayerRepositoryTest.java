package com.example.quizapp.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PlayerRepository Tests")
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    private Player player1;
    private Player player2;
    private Player inactivePlayer;

    @BeforeEach
    void setUp() {
        // Clear repository
        playerRepository.deleteAll();

        // Create test players
        player1 = Player.builder()
                .nickname("Alice")
                .sessionId(UUID.randomUUID().toString())
                .gamesPlayed(10)
                .active(true)
                .lastPlayedAt(LocalDateTime.now().minusDays(1))
                .build();

        player2 = Player.builder()
                .nickname("Bob")
                .sessionId(UUID.randomUUID().toString())
                .gamesPlayed(5)
                .active(true)
                .lastPlayedAt(LocalDateTime.now().minusDays(2))
                .build();

        inactivePlayer = Player.builder()
                .nickname("Inactive")
                .sessionId(UUID.randomUUID().toString())
                .gamesPlayed(3)
                .active(false)
                .lastPlayedAt(LocalDateTime.now().minusDays(10))
                .build();
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("Should save player successfully")
    void shouldSavePlayer() {
        // When
        Player savedPlayer = playerRepository.save(player1);

        // Then
        assertThat(savedPlayer).isNotNull();
        assertThat(savedPlayer.getId()).isNotNull();
        assertThat(savedPlayer.getNickname()).isEqualTo("Alice");
        assertThat(savedPlayer.getSessionId()).isNotNull();
        assertThat(savedPlayer.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should save player with default values")
    void shouldSavePlayerWithDefaults() {
        // Given
        Player simplePlayer = Player.builder()
                .nickname("Simple")
                .sessionId(UUID.randomUUID().toString())
                .build();

        // When
        Player savedPlayer = playerRepository.save(simplePlayer);

        // Then
        assertThat(savedPlayer.getGamesPlayed()).isZero();
        assertThat(savedPlayer.isActive()).isTrue();
    }

    // ==================== READ Tests ====================

    @Test
    @DisplayName("Should find player by ID")
    void shouldFindPlayerById() {
        // Given
        Player savedPlayer = playerRepository.save(player1);

        // When
        Optional<Player> foundPlayer = playerRepository.findById(savedPlayer.getId());

        // Then
        assertThat(foundPlayer).isPresent();
        assertThat(foundPlayer.get().getNickname()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should return empty when player not found")
    void shouldReturnEmptyWhenPlayerNotFound() {
        // When
        Optional<Player> foundPlayer = playerRepository.findById(999L);

        // Then
        assertThat(foundPlayer).isEmpty();
    }

    @Test
    @DisplayName("Should find all players")
    void shouldFindAllPlayers() {
        // Given
        playerRepository.save(player1);
        playerRepository.save(player2);
        playerRepository.save(inactivePlayer);

        // When
        List<Player> allPlayers = playerRepository.findAll();

        // Then
        assertThat(allPlayers).hasSize(3);
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("Should update player nickname")
    void shouldUpdatePlayerNickname() {
        // Given
        Player savedPlayer = playerRepository.save(player1);

        // When
        savedPlayer.setNickname("Alice Updated");
        Player updatedPlayer = playerRepository.save(savedPlayer);

        // Then
        assertThat(updatedPlayer.getNickname()).isEqualTo("Alice Updated");
    }

    @Test
    @DisplayName("Should update games played count")
    void shouldUpdateGamesPlayedCount() {
        // Given
        Player savedPlayer = playerRepository.save(player1);

        // When
        savedPlayer.setGamesPlayed(savedPlayer.getGamesPlayed() + 1);
        Player updatedPlayer = playerRepository.save(savedPlayer);

        // Then
        assertThat(updatedPlayer.getGamesPlayed()).isEqualTo(11);
    }

    @Test
    @DisplayName("Should update last played time")
    void shouldUpdateLastPlayedTime() {
        // Given
        Player savedPlayer = playerRepository.save(player1);
        LocalDateTime newTime = LocalDateTime.now();

        // When
        savedPlayer.setLastPlayedAt(newTime);
        Player updatedPlayer = playerRepository.save(savedPlayer);

        // Then
        assertThat(updatedPlayer.getLastPlayedAt()).isAfterOrEqualTo(newTime.minusSeconds(1));
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("Should delete player by ID")
    void shouldDeletePlayerById() {
        // Given
        Player savedPlayer = playerRepository.save(player1);
        Long playerId = savedPlayer.getId();

        // When
        playerRepository.deleteById(playerId);

        // Then
        Optional<Player> deletedPlayer = playerRepository.findById(playerId);
        assertThat(deletedPlayer).isEmpty();
    }

    @Test
    @DisplayName("Should delete player entity")
    void shouldDeletePlayerEntity() {
        // Given
        Player savedPlayer = playerRepository.save(player1);
        Long playerId = savedPlayer.getId();

        // When
        playerRepository.delete(savedPlayer);

        // Then
        Optional<Player> deletedPlayer = playerRepository.findById(playerId);
        assertThat(deletedPlayer).isEmpty();
    }

    // ==================== Custom Query Tests ====================
    // ONLY methods that exist in PlayerRepository!

    @Test
    @DisplayName("Should find players by nickname")
    void shouldFindPlayersByNickname() {
        // Given
        playerRepository.save(player1); // Alice
        playerRepository.save(player2); // Bob

        // When
        List<Player> found = playerRepository.findByNickname("Alice");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getNickname()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should find multiple players with same nickname")
    void shouldFindMultiplePlayersWithSameNickname() {
        // Given
        Player alice2 = Player.builder()
                .nickname("Alice")
                .sessionId(UUID.randomUUID().toString())
                .build();

        playerRepository.save(player1); // Alice
        playerRepository.save(alice2);   // Alice

        // When
        List<Player> found = playerRepository.findByNickname("Alice");

        // Then
        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("Should find player by session ID")
    void shouldFindPlayerBySessionId() {
        // Given
        Player savedPlayer = playerRepository.save(player1);

        // When
        Optional<Player> found = playerRepository.findBySessionId(savedPlayer.getSessionId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should return empty when session ID not found")
    void shouldReturnEmptyWhenSessionIdNotFound() {
        // When
        Optional<Player> found = playerRepository.findBySessionId("non-existent-session");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find only active players")
    void shouldFindOnlyActivePlayers() {
        // Given
        playerRepository.save(player1);        // active
        playerRepository.save(player2);        // active
        playerRepository.save(inactivePlayer); // inactive

        // When
        List<Player> activePlayers = playerRepository.findByActiveTrue();

        // Then
        assertThat(activePlayers).hasSize(2);
        assertThat(activePlayers)
                .extracting(Player::getNickname)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    @DisplayName("Should find top players by games played")
    void shouldFindTopPlayersByGamesPlayed() {
        // Given
        playerRepository.save(player1);  // 10 games
        playerRepository.save(player2);  // 5 games

        // When
        List<Player> topPlayers = playerRepository.findTopPlayersByGamesPlayed();

        // Then
        assertThat(topPlayers).hasSize(2);
        // Should be ordered by gamesPlayed DESC
        assertThat(topPlayers.get(0).getNickname()).isEqualTo("Alice");
        assertThat(topPlayers.get(0).getGamesPlayed()).isEqualTo(10);
        assertThat(topPlayers.get(1).getNickname()).isEqualTo("Bob");
        assertThat(topPlayers.get(1).getGamesPlayed()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should not include inactive players in top players")
    void shouldNotIncludeInactivePlayersInTopPlayers() {
        // Given
        playerRepository.save(player1);        // active, 10 games
        playerRepository.save(inactivePlayer); // inactive, 3 games

        // When
        List<Player> topPlayers = playerRepository.findTopPlayersByGamesPlayed();

        // Then
        assertThat(topPlayers).hasSize(1);
        assertThat(topPlayers.getFirst().getNickname()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should count active players")
    void shouldCountActivePlayers() {
        // Given
        playerRepository.save(player1);        // active
        playerRepository.save(player2);        // active
        playerRepository.save(inactivePlayer); // inactive

        // When
        long count = playerRepository.countByActiveTrue();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero when no active players")
    void shouldReturnZeroWhenNoActivePlayers() {
        // Given
        playerRepository.save(inactivePlayer); // inactive

        // When
        long count = playerRepository.countByActiveTrue();

        // Then
        assertThat(count).isZero();
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle player with zero games played")
    void shouldHandlePlayerWithZeroGamesPlayed() {
        // Given
        Player newPlayer = Player.builder()
                .nickname("Newbie")
                .sessionId(UUID.randomUUID().toString())
                .gamesPlayed(0)
                .build();

        // When
        Player saved = playerRepository.save(newPlayer);

        // Then
        assertThat(saved.getGamesPlayed()).isZero();
    }

    @Test
    @DisplayName("Should handle player deactivation")
    void shouldHandlePlayerDeactivation() {
        // Given
        Player savedPlayer = playerRepository.save(player1);

        // When
        savedPlayer.setActive(false);
        Player deactivated = playerRepository.save(savedPlayer);

        // Then
        assertThat(deactivated.isActive()).isFalse();

        // Verify not in active list
        List<Player> activePlayers = playerRepository.findByActiveTrue();
        assertThat(activePlayers).doesNotContain(deactivated);
    }

    @Test
    @DisplayName("Should handle unique session IDs")
    void shouldHandleUniqueSessionIds() {
        // Given
        Player saved1 = playerRepository.save(player1);
        Player saved2 = playerRepository.save(player2);

        // Then
        assertThat(saved1.getSessionId()).isNotEqualTo(saved2.getSessionId());
    }
}