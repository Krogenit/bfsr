package net.bfsr.server.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.faction.Faction;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.server.player.PacketSetPlayerShip;
import org.bson.types.ObjectId;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class Player {
    private final ObjectId id;
    @Setter
    private PlayerNetworkHandler networkHandler;
    private Ship playerShip;
    private final List<Ship> ships = new ArrayList<>();
    private final String username;
    private final Vector2f position = new Vector2f();
    @Setter
    private Faction faction;
    @Setter
    private byte[] digest;

    public Player(String username) {
        this.id = null;
        this.username = username;
    }

    public void setPlayerShip(Ship playerShip) {
        this.playerShip = playerShip;
        this.playerShip.setControlledByPlayer(true);
        networkHandler.sendTCPPacket(new PacketSetPlayerShip(playerShip.getId()));
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

    public void removeShip(Ship ship) {
        ships.remove(ship);
    }
}