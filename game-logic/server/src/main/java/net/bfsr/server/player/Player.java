package net.bfsr.server.player;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.server.dto.Default;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

@Getter
public class Player {
    private final String id;
    private final String username;

    private PlayerNetworkHandler networkHandler;
    private PlayerInputController playerInputController;

    @Setter
    private Faction faction;
    @Setter
    private Ship ship;

    @Default
    public Player(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public Player(String username) {
        this(null, username);
    }

    public void init(PlayerNetworkHandler networkHandler, PlayerInputController playerInputController) {
        this.networkHandler = networkHandler;
        this.playerInputController = playerInputController;
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