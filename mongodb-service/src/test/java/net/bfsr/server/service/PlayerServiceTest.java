package net.bfsr.server.service;

import net.bfsr.database.Main;
import net.bfsr.database.repository.PlayerRepository;
import net.bfsr.database.service.PlayerService;
import net.bfsr.faction.Faction;
import net.bfsr.server.dto.PlayerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Main.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
public class PlayerServiceTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.5");

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerService playerService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setup() {
        playerRepository.deleteAll().block();
    }

    @Test
    void addPlayer() {
        String username = "test";
        PlayerModel playerModel = playerService.save(new PlayerModel(null, username, Faction.HUMAN, new ArrayList<>())).block();
        assertThat(playerModel).isNotNull();
        assertThat(playerModel.name()).isEqualTo(username);
        assertThat(playerModel.id()).isNotNull();
        assertThat(playerModel.faction()).isEqualTo(Faction.HUMAN);
        assertThat(playerModel.ships().size()).isEqualTo(0);
    }

    @Test
    void getPlayerByName() {
        String username = "test";
        playerService.save(new PlayerModel(null, username, Faction.HUMAN, new ArrayList<>())).block();
        PlayerModel playerModel = playerService.getPlayer(username).block();
        assertThat(playerModel).isNotNull();
        assertThat(playerModel.name()).isEqualTo(username);
        assertThat(playerModel.id()).isNotNull();
        assertThat(playerModel.faction()).isEqualTo(Faction.HUMAN);
        assertThat(playerModel.ships().size()).isEqualTo(0);
    }

    @Test
    void deletePlayerByName() {
        String username = "test";
        playerService.save(new PlayerModel(null, username, Faction.HUMAN, new ArrayList<>())).block();
        playerService.deleteByName(username).block();
        assertThat(playerService.getPlayer(username).block()).isNull();
    }

    @Test
    void deleteAll() {
        playerService.save(new PlayerModel(null, "test", Faction.HUMAN, new ArrayList<>())).block();
        playerService.save(new PlayerModel(null, "test1", Faction.HUMAN, new ArrayList<>())).block();
        playerService.deleteAll().block();
        assertThat(playerService.getPlayer("test").block()).isNull();
        assertThat(playerService.getPlayer("test1").block()).isNull();
    }
}