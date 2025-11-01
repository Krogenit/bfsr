package net.bfsr.server.player;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.server.player.PacketSetPlayerShip;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.dto.Default;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

@Getter
public class Player {
    private final String id;
    private final String username;

    private PlayerNetworkHandler networkHandler;
    private PlayerInputController playerInputController;

    @Setter
    private Faction faction;
    private Ship ship;

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
        this.playerInputController = new PlayerInputController(this, networkHandler, entityTrackingManager, playerManager, aiFactory);
    }

    public void setShip(Ship ship, int frame) {
        this.ship = ship;
        playerInputController.setShip(ship);
        networkHandler.sendTCPPacket(new PacketSetPlayerShip(ship, frame));
    }

    public float getX() {
        return ship.getX();
    }

    public float getY() {
        return ship.getY();
    }

    public boolean canTrackEntity(RigidBody rigidBody) {
        return true;
    }
}