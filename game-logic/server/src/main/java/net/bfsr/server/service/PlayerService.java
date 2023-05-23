package net.bfsr.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.dto.converter.PlayerConverter;
import net.bfsr.server.player.Player;
import net.bfsr.server.rsocket.RSocketClient;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerConverter converter = Mappers.getMapper(PlayerConverter.class);
    private final RSocketClient databaseRSocketClient;

    public Player registerPlayer(String playerName, String password) {
        Player player = new Player(playerName);
        log.info("Registered new player {}", playerName);
        return player;
    }

    public Player authUser(String playerName, String password) {
        PlayerModel playerModel = databaseRSocketClient.request("player", playerName, PlayerModel.class).block();
        Player player;
        if (playerModel != null) {
            player = converter.from(playerModel);
        } else {
            player = registerPlayer(playerName, password);
        }

        return player;
    }

    public Mono<PlayerModel> save(Player player) {
        PlayerModel playerModel = converter.to(player);
        return databaseRSocketClient.request("save-player", playerModel, PlayerModel.class);
    }
}