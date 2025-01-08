package net.bfsr.server.event.listener.module;

import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.event.module.ModuleDestroyEvent;
import net.bfsr.network.packet.server.component.PacketDestroyModule;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;

public class ModuleEventListener {
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();

    @EventHandler
    public EventListener<ModuleDestroyEvent> moduleDestroyEvent() {
        return event -> {
            DamageableModule module = event.getModule();
            Ship ship = module.getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), player -> new PacketDestroyModule(ship.getId(), module.getId(),
                    module.getType(), player.getClientTime(ship.getWorld().getTimestamp())));
        };
    }
}