package net.bfsr.server.player;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.entity.RigidBody;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.faction.Faction;
import net.bfsr.network.GuiType;
import net.bfsr.network.packet.server.gui.PacketOpenGui;
import net.bfsr.server.database.PlayerRepository;

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

    public Player get(String username) {
        return playerRepository.load(username);
    }

    public void save(Player player) {
        playerRepository.save(player);
    }

    public void saveAllSync() {
        playerRepository.saveAllSync(players);
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

            if (ships.isEmpty() && lastAttacker != null) {
                String attacker = "";
                if (lastAttacker instanceof Ship attackerShip) {
                    attacker = attackerShip.getName();
                }
                player.getNetworkHandler().sendUDPPacket(new PacketOpenGui(GuiType.DESTROYED, attacker));
            }

            player.getPlayerInputController().update();
        }
    }

    public void respawnPlayer(World world, Player player, float x, float y) {
        Faction faction = player.getFaction();

        Ship playerShip;
        if (faction == Faction.HUMAN) {
            playerShip = shipFactory.createPlayerShipHumanSmall(world, x, y, random.nextFloat() * MathUtils.TWO_PI);
        } else if (faction == Faction.SAIMON) {
            playerShip = shipFactory.createPlayerShipSaimonSmall(world, x, y, random.nextFloat() * MathUtils.TWO_PI);
        } else {
            playerShip = shipFactory.createPlayerShipEngiSmall(world, x, y, random.nextFloat() * MathUtils.TWO_PI);
        }

        shipFactory.getShipOutfitter().outfit(playerShip);
        playerShip.setOwner(player.getUsername());
        playerShip.setName(player.getUsername());
        world.add(playerShip, false);

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
        ObjectIterator<Player> iterator = playerByShipMap.values().iterator();
        while (iterator.hasNext()) {
            Player player1 = iterator.next();
            if (player1 == player) {
                iterator.remove();
                break;
            }
        }
    }

    public void setPlayerControlledShip(Player player, Ship ship) {
        playerByShipMap.put(ship, player);
    }

    public void removePlayerControlledShip(Ship ship) {
        playerByShipMap.remove(ship);
    }

    public Player getPlayerControllingShip(Ship ship) {
        return playerByShipMap.get(ship);
    }

    public void clear() {
        players.clear();
        playerMap.clear();
        playerRepository.clear();
    }
}