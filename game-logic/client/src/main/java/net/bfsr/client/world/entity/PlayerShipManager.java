package net.bfsr.client.world.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.event.player.SetPlayerShipEvent;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.physics.correction.DynamicCorrectionHandler;
import net.bfsr.engine.physics.correction.HistoryCorrectionHandler;
import net.bfsr.engine.physics.correction.LocalPlayerInputCorrectionHandler;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;

public class PlayerShipManager {
    private static final int NO_SHIP_ID = -1;

    private final Client client;
    private final EventBus eventBus;
    private final LocalPlayerInputCorrectionHandler localPlayerInputCorrectionHandler = new LocalPlayerInputCorrectionHandler();

    private int shipId = NO_SHIP_ID;
    @Getter
    @Setter
    private Ship ship;

    public PlayerShipManager(Client client) {
        this.client = client;
        this.eventBus = client.getEventBus();
        this.eventBus.register(this);
    }

    public void update() {
        if (ship == null) {
            if (shipId == NO_SHIP_ID) {
                return;
            }

            RigidBody entity = client.getWorld().getEntityById(shipId);
            if (!(entity instanceof Ship ship)) {
                return;
            }

            if (this.ship != null) {
                this.ship.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.2f),
                        new CorrectionHandler(), new HistoryCorrectionHandler()));
                this.ship.setControlledByPlayer(false);
            }

            this.ship = ship;

            ship.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.2f),
                    localPlayerInputCorrectionHandler, localPlayerInputCorrectionHandler));
            ship.setControlledByPlayer(true);

            this.ship = ship;
            eventBus.publish(new SetPlayerShipEvent(ship));
        }

        localPlayerInputCorrectionHandler.setRenderDelayInFrames(client.getRenderDelayManager().getRenderDelayInFrames());

        if (ship.isDead()) {
            resetShip();
        }
    }

    public void setShipId(int shipId) {
        if (shipId == NO_SHIP_ID) {
            resetShip();
        } else {
            this.shipId = shipId;
        }
    }

    private void resetShip() {
        ship = null;
        shipId = NO_SHIP_ID;
        localPlayerInputCorrectionHandler.clear();
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> resetShip();
    }
}
