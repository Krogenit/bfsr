package net.bfsr.server.dedicated;

import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;

public class NoDatabasePlayerManager extends PlayerManager {
    @Override
    public Player login(String username, String password) {
        return new Player(username);
    }

    @Override
    public void save(Player player) {}

    @Override
    public void saveAllSync() {}
}