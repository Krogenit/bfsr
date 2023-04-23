package net.bfsr.server.controller;

import net.bfsr.database.Main;
import net.bfsr.database.repository.PlayerRepository;
import net.bfsr.faction.Faction;
import net.bfsr.server.dto.PlayerModel;
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

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@SpringBootTest(classes = Main.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
public class PlayerControllerTest {
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.5");

    private static RSocketRequester requester;

    @Autowired
    private PlayerRepository playerRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void setup(@Autowired RSocketRequester.Builder builder, @LocalRSocketServerPort Integer port, @Autowired RSocketStrategies strategies) {
        requester = builder.tcp("localhost", port);
    }

    @BeforeEach
    void setup() {
        playerRepository.deleteAll().block();
    }

    @Test
    void test() {
        String username = "Local Player";

        Mono<PlayerModel> voidMono = requester.route("save-player").data(new PlayerModel(null, username, Faction.HUMAN, new ArrayList<>())).retrieveMono(PlayerModel.class);

        StepVerifier
                .create(voidMono)
                .consumeNextWith(playerModel -> {
                    assertThat(playerModel.id()).isNotNull();
                    assertThat(playerModel.name()).isEqualTo(username);
                    assertThat(playerModel.faction()).isEqualTo(Faction.HUMAN);
                    assertThat(playerModel.ships().size()).isEqualTo(0);
                })
                .verifyComplete();

        Mono<PlayerModel> mono = requester.route("player").data(username).retrieveMono(PlayerModel.class);

        StepVerifier
                .create(mono)
                .consumeNextWith(playerModel -> {
                    assertThat(playerModel.id()).isNotNull();
                    assertThat(playerModel.name()).isEqualTo(username);
                    assertThat(playerModel.faction()).isEqualTo(Faction.HUMAN);
                    assertThat(playerModel.ships().size()).isEqualTo(0);
                })
                .verifyComplete();
    }
}