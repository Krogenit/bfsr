package net.bfsr.server.service;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.dto.converter.PlayerConverter;
import net.bfsr.server.player.Player;
import net.bfsr.server.rsocket.RSocketClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class PlayerService {
    private final TMap<String, Player> loadedPlayersMap = new THashMap<>();

    private final RSocketClient databaseRSocketClient;

    public Player registerPlayer(String playerName, String password) {
        Player player = new Player(playerName);
        log.info("Registered new player {}", playerName);
        return player;
    }

    public String authUser(String playerName, String password) {
        if (loadedPlayersMap.containsKey(playerName)) {
            return null;
        }

        PlayerModel playerModel = databaseRSocketClient.request("player", playerName, PlayerModel.class).block();
        Player player;
        if (playerModel != null) {
            player = PlayerConverter.INSTANCE.from(playerModel);
        } else {
            player = registerPlayer(playerName, password);
        }

        loadedPlayersMap.put(playerName, player);
        return null;
    }

    public Mono<PlayerModel> save(Player player) {
        PlayerModel playerModel = PlayerConverter.INSTANCE.to(player);
        return databaseRSocketClient.request("save-player", playerModel, PlayerModel.class);
    }

    public void saveAllSync() {
        List<Mono<PlayerModel>> monos = saveUsers();
        for (int i = 0; i < monos.size(); i++) {
            monos.get(i).block();
        }
    }

    public List<Mono<PlayerModel>> save() {
        return saveUsers();
    }

    private List<Mono<PlayerModel>> saveUsers() {
        List<Mono<PlayerModel>> monos = new ArrayList<>(loadedPlayersMap.size());
        loadedPlayersMap.forEachValue(object -> {
            monos.add(save(object));
            return true;
        });

        return monos;
    }

    public Player getPlayer(String username) {
        return loadedPlayersMap.get(username);
    }

    public void removePlayer(String username) {
        loadedPlayersMap.remove(username);
    }
}