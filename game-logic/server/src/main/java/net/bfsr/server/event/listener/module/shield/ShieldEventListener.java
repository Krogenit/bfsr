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
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;

public class ShieldEventListener {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();

    @EventHandler
    public EventListener<ShieldRebuildEvent> shieldRebuildEvent() {
        return event -> {
            Ship ship = event.shield().getShip();
            networkSystem.sendUDPPacketToAllNearby(new PacketShieldRebuild(ship.getId(), ship.getWorld().getTimestamp()),
                    ship.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }

    @EventHandler
    public EventListener<ShieldResetRebuildingTimeEvent> shieldResetRebuildingTimeEvent() {
        return event -> {
            Ship ship = event.shield().getShip();
            networkSystem.sendUDPPacketToAllNearby(
                    new PacketShieldRebuildingTime(ship.getId(), 0, ship.getWorld().getTimestamp()),
                    ship.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }

    @EventHandler
    public EventListener<ShieldRemoveEvent> shieldRemoveEvent() {
        return event -> {
            Ship ship = event.shield().getShip();
            networkSystem.sendUDPPacketToAllNearby(new PacketShieldRemove(ship.getId(), ship.getWorld().getTimestamp()),
                    ship.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }
}