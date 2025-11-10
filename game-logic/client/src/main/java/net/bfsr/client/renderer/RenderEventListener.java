package net.bfsr.client.renderer;

import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.renderer.entity.WreckRender;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.ship.ShipJumpInEvent;
import net.bfsr.event.entity.wreck.WreckDeathEvent;

public class RenderEventListener {
    private final Client client = Client.get();
    private final EntityRenderer entityRenderer = client.getEntityRenderer();

    @EventHandler
    public EventListener<ShipJumpInEvent> shipJumpInEvent() {
        return event -> {
            Ship ship = event.ship();
            ShipRender shipRender = entityRenderer.getRender(ship.getId());
            shipRender.onJumpIn();
        };
    }

    @EventHandler
    public EventListener<WreckDeathEvent> wreckDeathEventEvent() {
        return event -> {
            Wreck wreck = event.wreck();
            WreckRender wreckRender = entityRenderer.getRender(wreck.getId());
            wreckRender.onDeath();
        };
    }
}
