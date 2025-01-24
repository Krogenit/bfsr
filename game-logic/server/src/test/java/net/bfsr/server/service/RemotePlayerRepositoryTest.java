package net.bfsr.server.service;

import net.bfsr.database.Main;
import net.bfsr.faction.Faction;
import net.bfsr.server.database.RemotePlayerRepository;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.player.Player;
import net.bfsr.server.rsocket.RSocketClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Main.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
public class RemotePlayerRepositoryTest {
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.5");
    private RemotePlayerRepository playerRepository;
    private static final RSocketClient rSocketClient = new RSocketClient();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void setup(@Autowired RSocketRequester.Builder builder, @LocalRSocketServerPort Integer port,
                      @Autowired RSocketStrategies strategies) {
        rSocketClient.connect("localhost", port);
    }

    @BeforeEach
    void setup() {
        rSocketClient.fireAndForget("delete-all", Void.class).block();
        playerRepository = new RemotePlayerRepository(rSocketClient);
    }

    @Test
    void register() {
        String username = "test";
        Player player = playerRepository.register(username);
        assertThat(player).isNotNull();
        assertThat(player.getUsername()).isEqualTo(username);
        assertThat(player.getId()).isNull();
    }

    @Test
    void savePlayer() {
        Faction faction = Faction.HUMAN;
        String username = "test";

        Player player = playerRepository.register(username);
        player.setFaction(faction);
        Mono<PlayerModel> mono = playerRepository.saveInternal(player);

        StepVerifier.create(mono).expectNextCount(1).verifyComplete();
    }

    @Test
    void loadNewPlayer() {
        String username = "test";
        Player player = playerRepository.load(username);
        assertThat(player).isNotNull();
        assertThat(player.getUsername()).isEqualTo(username);
    }

    @Test
    void loadExistsPlayer() {
        String username = "test";
        Faction faction = Faction.HUMAN;
        Player player = new Player(username);
        player.setFaction(faction);
        playerRepository.saveInternal(player).block();

        Player authenticatedPlayer = playerRepository.load(username);
        assertThat(authenticatedPlayer).isNotNull();
        assertThat(authenticatedPlayer.getUsername()).isEqualTo(username);
        assertThat(authenticatedPlayer.getFaction()).isEqualTo(faction);
        assertThat(authenticatedPlayer.getId()).isNotNull();
        assertThat(authenticatedPlayer.getShips()).isNotNull();
        assertThat(authenticatedPlayer.getShips().size()).isEqualTo(0);
    }

    @Test
    void saveAllPlayers() {
        Player player = playerRepository.load("test");
        Player player1 = playerRepository.load("test1");
        List<Mono<PlayerModel>> monos = new ArrayList<>();
        monos.add(playerRepository.saveInternal(player));
        monos.add(playerRepository.saveInternal(player1));
        assertThat(monos.size()).isEqualTo(2);

        for (int i = 0; i < monos.size(); i++) {
            Mono<PlayerModel> mono = monos.get(i);
            StepVerifier.create(mono).expectNextCount(1).verifyComplete();
        }
    }
}