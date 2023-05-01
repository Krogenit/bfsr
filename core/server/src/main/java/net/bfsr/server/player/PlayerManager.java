package net.bfsr.server.player;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.GuiType;
import net.bfsr.server.core.Server;
import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.network.packet.server.gui.PacketOpenGui;
import net.bfsr.server.rsocket.RSocketClient;
import net.bfsr.server.service.PlayerService;
import net.bfsr.server.world.WorldServer;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    private final WorldServer world;
    @Getter
    private final List<Player> players = new ArrayList<>();
    private final TMap<String, Player> playerMap = new THashMap<>();
    private final RSocketClient databaseRSocketClient = new RSocketClient();
    @Getter
    private final PlayerService playerService = new PlayerService(databaseRSocketClient);

    public PlayerManager(WorldServer world) {
        this.world = world;
    }

    public void connect(String host, int port) {
        databaseRSocketClient.connect(host, port);
    }

    public void update() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            List<Ship> ships = player.getShips();
            RigidBody lastAttacker = null;
            for (int i1 = 0; i1 < ships.size(); i1++) {
                Ship ship = ships.get(i1);
                if (ship.isDead()) {
                    ships.remove(i1--);
                    lastAttacker = ship.getLastAttacker();
                }
            }

            if (ships.size() == 0 && lastAttacker != null) {
                String attacker = "";
                if (lastAttacker instanceof Ship attackerShip) {
                    attacker = attackerShip.getName();
                }
                player.getNetworkHandler().sendUDPPacket(new PacketOpenGui(GuiType.DESTROYED, attacker));
            }

            player.getPlayerInputController().update();
        }
    }

    public void respawnPlayer(Player player, float x, float y) {
        Faction faction = player.getFaction();
        Ship playerShip = null;
        switch (faction) {
            case HUMAN -> playerShip = ShipFactory.get().createPlayerShipHumanSmall(world, x, y, world.getRand().nextFloat() * MathUtils.TWO_PI);
            case SAIMON -> playerShip = ShipFactory.get().createPlayerShipSaimonSmall(world, x, y, world.getRand().nextFloat() * MathUtils.TWO_PI);
            case ENGI -> playerShip = ShipFactory.get().createPlayerShipEngiSmall(world, x, y, world.getRand().nextFloat() * MathUtils.TWO_PI);
        }

        ShipOutfitter.get().outfit(playerShip);
        playerShip.setOwner(player.getUsername());
        playerShip.setName(player.getUsername());
        world.addShip(playerShip);

        player.addShip(playerShip);
        player.setShip(playerShip);
    }

    public boolean hasPlayer(String username) {
        return playerMap.containsKey(username);
    }

    public void addPlayer(Player player) {
        players.add(player);
        playerMap.put(player.getUsername(), player);
    }

    public Player getPlayer(String username) {
        return playerMap.get(username);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        this.playerMap.remove(player.getUsername());
    }

    private List<Mono<PlayerModel>> saveUsers() {
        List<Mono<PlayerModel>> monos = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            monos.add(playerService.save(player));
        }

        return monos;
    }

    public void clear() {
        players.clear();
        playerMap.clear();
        databaseRSocketClient.clear();
    }

    public void save(Player player) {
        playerService.save(player);
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

    public static PlayerManager get() {
        return Server.getInstance().getPlayerManager();
    }
}