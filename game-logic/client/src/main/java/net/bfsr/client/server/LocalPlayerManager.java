package net.bfsr.client.server;

import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;

class LocalPlayerManager extends PlayerManager {
    @Override
    public Player login(String username, String password) {
        return new Player(username);
    }

    @Override
    public void save(Player player) {}

    @Override
    public void saveAllSync() {}
}
