package net.bfsr.server.player;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.faction.Faction;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.packet.server.PacketSetPlayerShip;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class PlayerServer {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private NetworkManagerServer networkManager;
    @Getter
    private Ship playerShip;
    @Getter
    private final List<Ship> ships = new ArrayList<>();
    @Getter
    private final String userName;
    @Getter
    private final String password;
    @Getter
    @Setter
    private int ping;
    @Getter
    private final Vector2f position = new Vector2f();
    @Getter
    @Setter
    private Faction faction;

    public PlayerServer(String playerName, String password) {
        this.userName = playerName;
        this.password = password;
    }

    public void setPlayerShip(Ship playerShip) {
        this.playerShip = playerShip;
        this.playerShip.setControlledByPlayer(true);
        networkManager.scheduleOutboundPacket(new PacketSetPlayerShip(playerShip.getId()));
    }

    public void setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
    }

    public void addShip(Ship ship) {
        this.ships.add(ship);
    }

    public Ship getShip(int i) {
        return ships.get(i);
    }

    public void removeShip(Ship s) {
        ships.remove(s);
    }
}
