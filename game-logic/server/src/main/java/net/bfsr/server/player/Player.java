package net.bfsr.server.player;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.world.entity.EntityIdManager;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.server.player.PacketSetPlayerShip;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.dto.Default;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Player {
    private final String id;
    private final List<Ship> ships = new ArrayList<>();
    private final String username;
    private final Vector2f position = new Vector2f();
    private final EntityIdManager localIdManager = new EntityIdManager(-1) {
        @Override
        public int getNextId() {
            return nextId--;
        }
    };

    private PlayerNetworkHandler networkHandler;
    private PlayerInputController playerInputController;

    @Setter
    private Faction faction;

    @Default
    public Player(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public Player(String username) {
        this(null, username);
    }

    public void init(PlayerNetworkHandler networkHandler, EntityTrackingManager entityTrackingManager, PlayerManager playerManager,
                     AiFactory aiFactory) {
        this.networkHandler = networkHandler;
        this.playerInputController = new PlayerInputController(this, entityTrackingManager, playerManager, aiFactory);
    }

    public void setShip(Ship ship) {
        playerInputController.setShip(ship);
        networkHandler.sendTCPPacket(new PacketSetPlayerShip(ship, ship.getWorld().getTimestamp()));
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

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public boolean canTrackEntity(RigidBody rigidBody) {
        return true;
    }
}