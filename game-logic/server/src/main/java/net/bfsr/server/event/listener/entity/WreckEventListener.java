package net.bfsr.server.event.listener.entity;

import clipper2.core.Path64;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.event.entity.wreck.BulletDamageShipWreckEvent;
import net.bfsr.server.ServerGameLogic;
import org.dyn4j.dynamics.Body;

public class WreckEventListener {
    private final DamageSystem damageSystem = ServerGameLogic.getInstance().getDamageSystem();

    @EventHandler
    public EventListener<BulletDamageShipWreckEvent> bulletDamageShipWreckEvent() {
        return event -> {
            ShipWreck wreck = event.wreck();
            Body body = wreck.getBody();
            float polygonRadius = 0.5f;
            float radius = 1.0f;

            double x = body.getTransform().getTranslationX();
            double y = body.getTransform().getTranslationY();
            double sin = body.getTransform().getSint();
            double cos = body.getTransform().getCost();

            Path64 clip = damageSystem.createCirclePath(event.contactX() - x, event.contactY() - y, -sin, cos, 12, polygonRadius);
            damageSystem.damage(wreck, event.contactX(), event.contactY(), clip, radius);
        };
    }
}