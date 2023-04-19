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
public class Player {
    @Getter
    private final ObjectId id;
    @Getter
    @Setter
    private PlayerNetworkHandler networkHandler;
    @Getter
    private Ship playerShip;
    @Getter
    private final List<Ship> ships = new ArrayList<>();
    @Getter
    private final String username;
    @Getter
    private final Vector2f position = new Vector2f();
    @Getter
    @Setter
    private Faction faction;
    @Getter
    @Setter
    private byte[] digest;

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