package net.bfsr.server.player;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.faction.Faction;
import net.bfsr.network.GuiType;
import net.bfsr.network.packet.server.gui.PacketOpenGui;
import net.bfsr.network.packet.server.player.PacketSetPlayerShip;
import net.bfsr.server.database.PlayerRepository;
import net.bfsr.server.entity.ship.ShipSpawner;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PlayerManager {
    @Getter
    protected final List<Player> players = new ArrayList<>();
    private final TMap<String, Player> playerMap = new THashMap<>();
    private final Object2ObjectMap<Ship, Player> playerByShipMap = new Object2ObjectOpenHashMap<>();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final ShipFactory shipFactory;

    private PlayerRepository playerRepository;

    public void init(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void joinGame(World world, Player player, ShipSpawner shipSpawner, int frame) {
        Ship ship = player.getShip();
        if (ship == null) {
            respawnPlayer(world, player, 0, 0, frame, shipSpawner);
        } else {
            initShips(player, world);
            spawnShips(player, world, shipSpawner);
            setShip(player, ship, frame);
        }
    }

    public void respawnPlayer(World world, Player player, float x, float y, int frame, ShipSpawner shipSpawner) {
        Ship ship = createPlayerShip(world, x, y, random.nextFloat() * MathUtils.TWO_PI, player);
        setShip(player, ship, frame);
        shipSpawner.spawnShip(world, ship);
    }

    private Ship createPlayerShip(World world, float x, float y, float angle, Player player) {
        Faction faction = player.getFaction();

        Ship playerShip = switch (faction) {
            case HUMAN -> shipFactory.createPlayerShipHumanSmall(world, x, y, angle);
            case SAIMON -> shipFactory.createPlayerShipSaimonSmall(world, x, y, angle);
            case ENGI -> shipFactory.createPlayerShipEngiSmall(world, x, y, angle);
        };

        initShip(player, playerShip);
        return playerShip;
    }

    private void initShips(Player player, World world) {
        Ship ship = player.getShip();
        shipFactory.initShip(ship, world, world.getNextId());
        ship.setFaction(player.getFaction());
        initShip(player, ship);
    }

    private void initShip(Player player, Ship ship) {
        ship.setName(player.getUsername());
        ship.setOwner(player.getUsername());
        shipFactory.getShipOutfitter().outfit(ship);
    }

    private void spawnShips(Player player, World world, ShipSpawner shipSpawner) {
        Ship ship = player.getShip();
        shipSpawner.spawnShip(world, ship);
    }

    private void setShip(Player player, Ship ship, int frame) {
        playerByShipMap.put(ship, player);
        player.setShip(ship);
        player.getPlayerInputController().setShip(ship);
        player.getNetworkHandler().sendTCPPacket(new PacketSetPlayerShip(ship, frame));
    }

    public void update(int frame) {
        for (int i = 0; i < players.size(); i++) {
            updatePlayer(players.get(i), frame);
        }
    }

    private void updatePlayer(Player player, int frame) {
        player.getPlayerInputController().update(frame);
        updatePlayerShips(player, frame);
    }

    private void updatePlayerShips(Player player, int frame) {
        RigidBody lastAttacker = null;
        Ship ship = player.getShip();
        if (ship == null) {
            return;
        }

        if (ship.isDead()) {
            setShip(player, ship, frame);
            lastAttacker = ship.getLastAttacker();
        }

        if (player.getShip() == null && lastAttacker != null) {
            String attacker = "";
            if (lastAttacker instanceof Ship attackerShip) {
                attacker = attackerShip.getName();
            }
            player.getNetworkHandler().sendTCPPacket(new PacketOpenGui(GuiType.DESTROYED, attacker));
        }
    }

    public Player get(String username) {
        return playerRepository.load(username);
    }

    public void save(Player player) {
        playerRepository.save(player);
    }

    public void saveAllSync() {
        playerRepository.saveAllSync(players);
    }

    public Player getPlayerControllingShip(Ship ship) {
        return playerByShipMap.get(ship);
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
        ObjectIterator<Player> iterator = playerByShipMap.values().iterator();
        while (iterator.hasNext()) {
            Player player1 = iterator.next();
            if (player1 == player) {
                iterator.remove();
                break;
            }
        }
    }

    public void clear() {
        players.clear();
        playerMap.clear();
        playerRepository.clear();
    }
}