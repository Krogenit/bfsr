package net.bfsr.server.database;

import net.bfsr.config.ConfigLoader;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.dto.converter.PlayerConverter;
import net.bfsr.server.player.Player;
import org.mapstruct.factory.Mappers;

import java.nio.file.Path;
import java.util.List;

public class FileSystemPlayerRepository implements PlayerRepository {
    private final Path folder = Path.of("players");
    private final PlayerConverter playerConverter = Mappers.getMapper(PlayerConverter.class);

    @Override
    public Player load(String username) {
        Path filePath = folder.resolve(username + ".json");

        if (filePath.toFile().exists()) {
            PlayerModel playerModel = ConfigLoader.load(filePath, PlayerModel.class);
            return playerConverter.from(playerModel);
        } else {
            return new Player(username);
        }
    }

    @Override
    public void save(Player player) {
        PlayerModel playerModel = playerConverter.to(player);
        Path filePath = folder.resolve(player.getUsername() + ".json");
        ConfigLoader.save(filePath, playerModel);
    }

    @Override
    public void saveAllSync(List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            save(players.get(i));
        }
    }

    @Override
    public void clear() {}
}
