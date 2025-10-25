package net.bfsr.server.event.listener.module.weapon;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import net.bfsr.network.packet.server.component.PacketWeaponSlotRemove;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;

@RequiredArgsConstructor
public class WeaponEventListener {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();
    private final EntityTrackingManager trackingManager;

    @EventHandler
    public EventListener<WeaponSlotRemovedEvent> weaponSlotRemovedEvent() {
        return event -> {
            WeaponSlot weaponSlot = event.getWeaponSlot();
            Ship ship = weaponSlot.getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketWeaponSlotRemove(ship.getId(), weaponSlot.getId(),
                    gameLogic.getFrame()));
        };
    }
}