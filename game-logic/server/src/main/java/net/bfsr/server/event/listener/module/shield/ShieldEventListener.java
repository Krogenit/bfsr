package net.bfsr.server.event.listener.module.shield;

import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.shield.ShieldRebuildEvent;
import net.bfsr.event.module.shield.ShieldRemoveEvent;
import net.bfsr.event.module.shield.ShieldResetRebuildingTimeEvent;
import net.bfsr.network.packet.server.component.PacketShieldRebuild;
import net.bfsr.network.packet.server.component.PacketShieldRebuildingTime;
import net.bfsr.network.packet.server.component.PacketShieldRemove;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;

public class ShieldEventListener {
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();

    @EventHandler
    public EventListener<ShieldRebuildEvent> shieldRebuildEvent() {
        return event -> {
            Ship ship = event.shield().getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRebuild(ship.getId(),
                    ship.getWorld().getTimestamp()));
        };
    }

    @EventHandler
    public EventListener<ShieldResetRebuildingTimeEvent> shieldResetRebuildingTimeEvent() {
        return event -> {
            Ship ship = event.shield().getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRebuildingTime(ship.getId(), 0,
                    ship.getWorld().getTimestamp()));
        };
    }

    @EventHandler
    public EventListener<ShieldRemoveEvent> shieldRemoveEvent() {
        return event -> {
            Ship ship = event.shield().getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(),
                    new PacketShieldRemove(ship.getId(), ship.getWorld().getTimestamp()));
        };
    }
}