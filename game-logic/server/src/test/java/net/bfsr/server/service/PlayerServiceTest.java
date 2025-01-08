package net.bfsr.server.service;

import net.bfsr.database.Main;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.faction.Faction;
import net.bfsr.server.dedicated.DedicatedServerGameLogic;
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
public class PlayerServiceTest {
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.5");
    private PlayerService playerService;
    private static final RSocketClient rSocketClient = new RSocketClient();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void setup(@Autowired RSocketRequester.Builder builder, @LocalRSocketServerPort Integer port,
                      @Autowired RSocketStrategies strategies) {
        rSocketClient.connect("localhost", port);
        new DedicatedServerGameLogic(new Profiler());
    }

    @BeforeEach
    void setup() {
        rSocketClient.fireAndForget("delete-all", Void.class).block();
        playerService = new PlayerService(rSocketClient);
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
    void savePlayer() {
        Faction faction = Faction.HUMAN;
        String username = "test";

        Player player = playerService.registerPlayer(username, "");
        player.setFaction(faction);
        Mono<PlayerModel> mono = playerService.save(player);

        StepVerifier.create(mono).expectNextCount(1).verifyComplete();
    }

    @Test
    void authNewPlayer() {
        String username = "test";
        Player player = playerService.authUser(username, "");
        assertThat(player).isNotNull();
        assertThat(player.getUsername()).isEqualTo(username);
    }

    @Test
    void authExistsPlayer() {
        String username = "test";
        Faction faction = Faction.HUMAN;
        Player player = new Player(username);
        player.setFaction(faction);
        playerService.save(player).block();

        Player authenticatedPlayer = playerService.authUser(username, "");
        assertThat(authenticatedPlayer).isNotNull();
        assertThat(authenticatedPlayer.getUsername()).isEqualTo(username);
        assertThat(authenticatedPlayer.getFaction()).isEqualTo(faction);
        assertThat(authenticatedPlayer.getId()).isNotNull();
        assertThat(authenticatedPlayer.getShips()).isNotNull();
        assertThat(authenticatedPlayer.getShips().size()).isEqualTo(0);
    }

    @Test
    void saveAllPlayers() {
        Player player = playerService.authUser("test", "");
        Player player1 = playerService.authUser("test1", "");
        List<Mono<PlayerModel>> monos = new ArrayList<>();
        monos.add(playerService.save(player));
        monos.add(playerService.save(player1));
        assertThat(monos.size()).isEqualTo(2);

        for (int i = 0; i < monos.size(); i++) {
            Mono<PlayerModel> mono = monos.get(i);
            StepVerifier.create(mono).expectNextCount(1).verifyComplete();
        }
    }
}