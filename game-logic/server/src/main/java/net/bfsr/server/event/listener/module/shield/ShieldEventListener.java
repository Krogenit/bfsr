package net.bfsr.server.event.listener.module.shield;

import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.shield.ShieldRebuildEvent;
import net.bfsr.event.module.shield.ShieldRemoveEvent;
import net.bfsr.event.module.shield.ShieldResetRebuildingTimeEvent;
import net.bfsr.network.packet.server.component.PacketShieldRebuild;
import net.bfsr.network.packet.server.component.PacketShieldRebuildingTime;
import net.bfsr.network.packet.server.component.PacketShieldRemove;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.util.TrackingUtils;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class ShieldEventListener {
    @Handler
    public void event(ShieldRebuildEvent event) {
        Ship ship = event.shield().getShip();
        ServerGameLogic.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShieldRebuild(ship.getId()), ship.getPosition(), TrackingUtils.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(ShieldResetRebuildingTimeEvent event) {
        Ship ship = event.shield().getShip();
        ServerGameLogic.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShieldRebuildingTime(ship.getId(), 0), ship.getPosition(), TrackingUtils.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(ShieldRemoveEvent event) {
        Ship ship = event.shield().getShip();
        ServerGameLogic.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShieldRemove(ship.getId()), ship.getPosition(), TrackingUtils.PACKET_SPAWN_DISTANCE);
    }
}