package net.bfsr.server.event.listener.module;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.event.module.ModuleDestroyEvent;
import net.bfsr.network.packet.server.component.PacketDestroyModule;
import net.bfsr.server.entity.EntityTrackingManager;

@RequiredArgsConstructor
public class ModuleEventListener {
    private final EntityTrackingManager trackingManager;

    @EventHandler
    public EventListener<ModuleDestroyEvent> moduleDestroyEvent() {
        return event -> {
            DamageableModule module = event.getModule();
            Ship ship = module.getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketDestroyModule(ship.getId(), module.getId(),
                    module.getType(), ship.getWorld().getTimestamp()));
        };
    }
}