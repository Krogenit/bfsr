package net.bfsr.server.database;

import net.bfsr.server.player.Player;

import java.util.List;

public interface PlayerRepository {
    Player load(String username);
    void save(Player player);
    void saveAllSync(List<Player> players);
    void clear();
}
