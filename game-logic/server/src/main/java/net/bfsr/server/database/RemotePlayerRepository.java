package net.bfsr.server.database;

import lombok.extern.log4j.Log4j2;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.dto.converter.PlayerConverter;
import net.bfsr.server.player.Player;
import net.bfsr.server.rsocket.RSocketClient;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RemotePlayerRepository implements PlayerRepository {
    private final PlayerConverter converter = Mappers.getMapper(PlayerConverter.class);
    private final RSocketClient databaseRSocketClient;

    public RemotePlayerRepository(RSocketClient databaseRSocketClient) {
        this.databaseRSocketClient = databaseRSocketClient;
    }

    public RemotePlayerRepository(ServerSettings settings) {
        this(new RSocketClient());
        databaseRSocketClient.connect(settings.getDataBaseServiceHost(), settings.getDatabaseServicePort());
    }

    public Player register(String username) {
        Player player = new Player(username);
        log.info("Registered new player {}", username);
        return player;
    }

    @Override
    public Player load(String username) {
        PlayerModel playerModel = databaseRSocketClient.request("player", username, PlayerModel.class).block();
        Player player;
        if (playerModel != null) {
            player = converter.from(playerModel);
        } else {
            player = register(username);
        }

        return player;
    }

    @Override
    public void save(Player player) {
        saveInternal(player);
    }

    public Mono<PlayerModel> saveInternal(Player player) {
        PlayerModel playerModel = converter.to(player);
        return databaseRSocketClient.request("save-player", playerModel, PlayerModel.class);
    }

    @Override
    public void saveAllSync(List<Player> players) {
        List<Mono<PlayerModel>> monoList = saveUsers(players);
        for (int i = 0; i < monoList.size(); i++) {
            monoList.get(i).block();
        }
    }

    private List<Mono<PlayerModel>> saveUsers(List<Player> players) {
        List<Mono<PlayerModel>> monoList = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            monoList.add(saveInternal(player));
        }

        return monoList;
    }

    @Override
    public void clear() {
        databaseRSocketClient.clear();
    }
}
