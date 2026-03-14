package net.bfsr.server.player;

import lombok.Getter;
import net.bfsr.engine.ai.Ai;
import net.bfsr.entity.ship.Ship;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import org.joml.Vector2f;

public abstract class PlayerInputController {
    protected final ServerGameLogic gameLogic = ServerGameLogic.get();
    protected final Player player;
    protected final PlayerNetworkHandler networkHandler;
    final Vector2f mousePosition = new Vector2f();
    final boolean[] mouseStates = {false, false};
    final boolean[] buttonsStates = {false, false, false, false, false};
    final EntityTrackingManager trackingManager;
    protected final AiFactory aiFactory;

    @Getter
    protected Ship ship;

    PlayerInputController(Player player, PlayerNetworkHandler networkHandler, EntityTrackingManager trackingManager,
                          AiFactory aiFactory) {
        this.player = player;
        this.networkHandler = networkHandler;
        this.trackingManager = trackingManager;
        this.aiFactory = aiFactory;
    }

    public abstract void update(int frame);

    public void setMousePosition(float mouseWorldX, float mouseWorldY) {
        mousePosition.set(mouseWorldX, mouseWorldY);
    }

    public void setShip(Ship ship) {
        if (this.ship != null) {
            this.ship.setAi(aiFactory.createAi());
            this.ship.removeAllMoveDirections();
            this.ship.setControlledByPlayer(false);
        }

        this.ship = ship;

        if (ship != null) {
            ship.setAi(Ai.NO_AI);
            ship.removeAllMoveDirections();
            ship.setControlledByPlayer(true);
        }
    }

    public void setInputStates(boolean[] mouseStates, boolean[] buttonsStates) {
        for (int i = 0; i < this.mouseStates.length; i++) {
            this.mouseStates[i] = mouseStates[i];
        }

        for (int i = 0; i < this.buttonsStates.length; i++) {
            this.buttonsStates[i] = buttonsStates[i];
        }
    }

    public void toggleLeftClickInputState() {
        buttonsStates[0] = !buttonsStates[0];
    }
}