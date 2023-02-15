package net.bfsr.server.player;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.faction.Faction;
import net.bfsr.server.entity.Ship;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.packet.server.PacketSetPlayerShip;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class PlayerServer {
    @Getter
    @Setter
    private NetworkManagerServer networkManager;
    private Ship playerShip;
    private final List<Ship> ships = new ArrayList<>();
    private final String userName;
    private final String password;
    private int ping, id;
    private final Vector2f position = new Vector2f();
    private Faction faction;

    public PlayerServer(String playerName, String password) {
        this.userName = playerName;
        this.password = password;
    }

    public Ship getPlayerShip() {
        return playerShip;
    }

    public void setPlayerShip(Ship playerShip) {
        this.playerShip = playerShip;
        this.playerShip.setControlledByPlayer(true);
        networkManager.scheduleOutboundPacket(new PacketSetPlayerShip(playerShip.getId()));
    }

    public String getUserName() {
        return userName;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public int getPing() {
        return ping;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public void addShip(Ship ship) {
        this.ships.add(ship);
    }

    public ShipCommon getShip(int i) {
        return ships.get(i);
    }

    public List<Ship> getShips() {
        return ships;
    }

    public void removeShip(ShipCommon s) {
        ships.remove(s);
    }
}
