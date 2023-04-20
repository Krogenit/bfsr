package net.bfsr.server.service;

import net.bfsr.faction.Faction;
import net.bfsr.server.DedicatedServerSpringApplication;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.player.Player;
import net.bfsr.server.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = DedicatedServerSpringApplication.class)
public class PlayerServiceTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.5");
    @Autowired
    private PlayerRepository playerRepository;
    private PlayerService playerService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setup() {
        playerRepository.deleteAll();
        playerService = new PlayerService(playerRepository);
    }

    @Test
    void registerPlayer() {
        String username = "test";
        Player player = playerService.registerPlayer(username, "");
        assertThat(player).isNotNull();
        assertThat(player.getUsername()).isEqualTo(username);
        assertThat(player.getId()).isNull();
    }

    @Test
    void saveSimplePlayer() {
        Faction faction = Faction.HUMAN;
        String username = "test";

        Player player = playerService.registerPlayer(username, "");
        player.setFaction(faction);
        playerService.save(player);

        assertThat(playerRepository.findAll().size()).isEqualTo(1);

        PlayerModel playerModel = playerRepository.findByName(username);

        assertThat(playerModel).isNotNull();
        assertThat(playerModel.id()).isNotNull();
        assertThat(playerModel.name()).isEqualTo(username);
        assertThat(playerModel.faction()).isEqualTo(faction);
        assertThat(playerModel.ships()).isNotNull();
        assertThat(playerModel.ships().size()).isEqualTo(0);
    }

    @Test
    void authNewPlayer() {
        String username = "test";
        String result = playerService.authUser(username, "");
        assertThat(result).isNull();
        Player player = playerService.getPlayer(username);
        assertThat(player).isNotNull();
        assertThat(player.getUsername()).isEqualTo(username);
    }

    @Test
    void authExistsPlayer() {
        String username = "test";
        Faction faction = Faction.HUMAN;
        Player player = new Player(username);
        player.setFaction(faction);
        playerService.save(player);

        String result = playerService.authUser(username, "");
        assertThat(result).isNull();

        Player authenticatedPlayer = playerService.getPlayer(username);
        assertThat(authenticatedPlayer).isNotNull();
        assertThat(authenticatedPlayer.getUsername()).isEqualTo(username);
        assertThat(authenticatedPlayer.getFaction()).isEqualTo(faction);
        assertThat(authenticatedPlayer.getId()).isNotNull();
        assertThat(authenticatedPlayer.getShips()).isNotNull();
        assertThat(authenticatedPlayer.getShips().size()).isEqualTo(0);
    }

    @Test
    void saveAllPlayers() {
        playerService.authUser("test", "");
        playerService.authUser("test1", "");
        playerService.save();
        assertThat(playerRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void removePlayerFromCache() {
        String username = "test";
        playerService.authUser(username, "");
        playerService.removePlayer(username);
        assertThat(playerService.getPlayer(username)).isNull();
    }
}