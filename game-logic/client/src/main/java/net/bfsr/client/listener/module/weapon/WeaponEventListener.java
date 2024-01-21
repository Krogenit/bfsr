package net.bfsr.client.listener.module.weapon;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.module.weapon.WeaponShotEvent;

public class WeaponEventListener {
    private final RenderManager renderManager = Core.get().getRenderManager();

    @EventHandler
    public EventListener<WeaponShotEvent> weaponShotEvent() {
        return event -> {
            WeaponSlot weaponSlot = event.weaponSlot();
            Ship ship = weaponSlot.getShip();
            if (ship.isControlledByPlayer()) {
                weaponSlot.createBullet(0, renderManager::createRender);
            }
        };
    }
}