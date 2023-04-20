package net.bfsr.server.service;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.core.SpringContext;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.dto.converter.PlayerConverter;
import net.bfsr.server.player.Player;
import net.bfsr.server.repository.PlayerRepository;

@Log4j2
public class PlayerService {
    private final TMap<String, Player> loadedPlayersMap = new THashMap<>();

    private final PlayerRepository playerRepository;

    public PlayerService() {
        this.playerRepository = SpringContext.getBean(PlayerRepository.class);
    }

    public Player registerPlayer(String playerName, String password) {
        Player player = new Player(playerName);
        log.info("Registered new player {}", playerName);
        return player;
    }

    public String authUser(String playerName, String password) {
        if (loadedPlayersMap.containsKey(playerName)) {
            return null;
        }

        PlayerModel playerModel = playerRepository.findByName(playerName);
        Player player;
        if (playerModel != null) {
            player = PlayerConverter.INSTANCE.from(playerModel);
        } else {
            player = registerPlayer(playerName, password);
        }

        loadedPlayersMap.put(playerName, player);
        return null;
    }

    public void save(Player player) {
        PlayerModel playerModel = PlayerConverter.INSTANCE.to(player);
        playerRepository.save(playerModel);
    }

    public void save() {
        saveUsers();
    }

    private void saveUsers() {
        loadedPlayersMap.forEachValue(object -> {
            save(object);
            return true;
        });
    }

    public Player getPlayer(String username) {
        return loadedPlayersMap.get(username);
    }

    public void removePlayer(String username) {
        loadedPlayersMap.remove(username);
    }
}