package net.bfsr.server.event.listener.module;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.event.module.ModuleAddEvent;
import net.bfsr.event.module.ModuleDestroyEvent;
import net.bfsr.network.packet.server.component.PacketModuleAdd;
import net.bfsr.network.packet.server.component.PacketModuleRemove;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;

@RequiredArgsConstructor
public class ModuleEventListener {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();
    private final EntityTrackingManager trackingManager;

    @EventHandler
    public EventListener<ModuleAddEvent> moduleAddEvent() {
        return event -> {
            Ship ship = event.getShip();
            Module module = event.getModule();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(),
                    new PacketModuleAdd(ship.getId(), module, gameLogic.getFrame()));
        };
    }

    @EventHandler
    public EventListener<ModuleDestroyEvent> moduleDestroyEvent() {
        return event -> {
            DamageableModule module = event.getModule();
            Ship ship = module.getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketModuleRemove(ship.getId(), module.getId(),
                    module.getType(), gameLogic.getFrame()));
        };
    }
}