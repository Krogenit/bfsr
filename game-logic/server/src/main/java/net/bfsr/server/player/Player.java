package net.bfsr.server.player;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.server.player.PacketSetPlayerShip;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.dto.Default;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Player {
    private final String id;
    @Setter
    private PlayerNetworkHandler networkHandler;
    private final List<Ship> ships = new ArrayList<>();
    private final String username;
    private final Vector2f position = new Vector2f();
    @Setter
    private Faction faction;
    private final PlayerInputController playerInputController;

    @Default
    public Player(String id, String username) {
        this.id = id;
        this.username = username;
        this.playerInputController = new PlayerInputController();
    }

    public Player(String username) {
        this(null, username);
    }

    public void setShip(Ship ship) {
        playerInputController.setShip(ship);
        ServerGameLogic.getInstance().getPlayerManager().setPlayerControlledShip(this, ship);
        networkHandler.sendTCPPacket(new PacketSetPlayerShip(ship.getId(), ship.getWorld().getTimestamp()));
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