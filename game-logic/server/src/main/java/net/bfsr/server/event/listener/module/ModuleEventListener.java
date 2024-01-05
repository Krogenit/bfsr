package net.bfsr.server.event.listener.module;

import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.event.module.ModuleDestroyEvent;
import net.bfsr.network.packet.server.component.PacketDestroyModule;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.joml.Vector2f;

@Listener(references = References.Strong)
public class ModuleEventListener {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();

    @Handler
    public void event(ModuleDestroyEvent event) {
        DamageableModule module = event.getModule();
        Ship ship = module.getShip();
        Vector2f position = ship.getPosition();
        networkSystem.sendTCPPacketToAllNearby(new PacketDestroyModule(ship.getId(), module.getId(), module.getType()),
                position.x, position.y, TrackingUtils.TRACKING_DISTANCE);
    }
}