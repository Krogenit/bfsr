package net.bfsr.server.dedicated;

import lombok.Getter;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;
import net.bfsr.server.rsocket.RSocketClient;
import net.bfsr.server.service.PlayerService;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class DedicatedPlayerManager extends PlayerManager {
    private final RSocketClient databaseRSocketClient = new RSocketClient();
    @Getter
    private final PlayerService playerService = new PlayerService(databaseRSocketClient);

    @Override
    public void init(ServerSettings settings) {
        databaseRSocketClient.connect(settings.getDataBaseServiceHost(), settings.getDatabaseServicePort());
    }

    @Override
    public Player login(String username, String password) {
        return playerService.authUser(username, password);
    }

    @Override
    public void save(Player player) {
        playerService.save(player);
    }

    @Override
    public void saveAllSync() {
        List<Mono<PlayerModel>> monos = saveUsers();
        for (int i = 0; i < monos.size(); i++) {
            monos.get(i).block();
        }
    }

    private List<Mono<PlayerModel>> saveUsers() {
        List<Mono<PlayerModel>> monos = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            monos.add(playerService.save(player));
        }

        return monos;
    }

    @Override
    public void clear() {
        super.clear();
        databaseRSocketClient.clear();
    }
}
