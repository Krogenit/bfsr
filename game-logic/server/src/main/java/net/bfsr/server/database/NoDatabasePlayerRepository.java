package net.bfsr.server.database;

import net.bfsr.server.player.Player;

import java.util.List;

public class NoDatabasePlayerRepository implements PlayerRepository {

    @Override
    public Player load(String username) {
        return new Player(username);
    }

    @Override
    public void save(Player player) {}

    @Override
    public void saveAllSync(List<Player> players) {}

    @Override
    public void clear() {}
}
