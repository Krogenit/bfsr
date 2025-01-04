package net.bfsr.server.event.listener.module.weapon;

import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import net.bfsr.network.packet.server.component.PacketRemoveWeaponSlot;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.world.World;

public class WeaponEventListener {
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();
    private final World world = ServerGameLogic.getInstance().getWorld();

    @EventHandler
    public EventListener<WeaponSlotRemovedEvent> weaponSlotRemovedEvent() {
        return event -> {
            WeaponSlot weaponSlot = event.getWeaponSlot();
            Ship ship = weaponSlot.getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), player -> new PacketRemoveWeaponSlot(ship.getId(),
                    weaponSlot.getId(), player.getClientTime(world.getTimestamp())));
        };
    }
}