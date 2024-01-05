package net.bfsr.client.listener.module.weapon;

import net.bfsr.client.Core;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.component.WeaponSlotBeamRender;
import net.bfsr.client.renderer.component.WeaponSlotRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class BeamEventListener {
    private final RenderManager renderManager = Core.get().getRenderManager();

    @Handler
    public void event(BeamDamageShipShieldEvent event) {
        WeaponSlotBeam slot = event.getSlot();
        Render<?> render = renderManager.getRender(slot.getShip().getId());
        if (render instanceof ShipRender shipRender) {
            WeaponSlotRender<? extends WeaponSlot> weaponSlotRender = shipRender.getWeaponRender(slot.getId());
            if (weaponSlotRender instanceof WeaponSlotBeamRender weaponSlotBeamRender) {
                weaponSlotBeamRender.onDamage(event.getRaycast(), event.getHitX(), event.getHitY());
            }
        }
    }

    @Handler
    public void event(BeamDamageShipArmorEvent event) {
        WeaponSlotBeam slot = event.getSlot();
        Ship ship = event.getShip();
        Render<?> render = renderManager.getRender(slot.getShip().getId());
        if (render instanceof ShipRender shipRender) {
            WeaponSlotRender<? extends WeaponSlot> weaponSlotRender = shipRender.getWeaponRender(slot.getId());
            if (weaponSlotRender instanceof WeaponSlotBeamRender weaponSlotBeamRender) {
                weaponSlotBeamRender.onDamage(event.getRaycast(), event.getHitX(), event.getHitY());
            }
        }

        GarbageSpawner.beamArmorDamage(event.getHitX(), event.getHitY(), ship.getVelocity().x * 0.005f,
                ship.getVelocity().y * 0.005f);
    }

    @Handler
    public void event(BeamDamageShipHullEvent event) {
        WeaponSlotBeam slot = event.getSlot();
        Ship ship = event.getShip();
        Render<?> render = renderManager.getRender(slot.getShip().getId());
        if (render instanceof ShipRender shipRender) {
            WeaponSlotRender<? extends WeaponSlot> weaponSlotRender = shipRender.getWeaponRender(slot.getId());
            if (weaponSlotRender instanceof WeaponSlotBeamRender weaponSlotBeamRender) {
                weaponSlotBeamRender.onDamage(event.getRaycast(), event.getHitX(), event.getHitY());
            }
        }

        GarbageSpawner.beamHullDamage(event.getHitX(), event.getHitY(), ship.getVelocity().x * 0.005f,
                ship.getVelocity().y * 0.005f);
    }

    @Handler
    public void event(BeamDamageWreckEvent event) {
        Wreck wreck = event.wreck();
        WeaponSlotBeam slot = event.slotBeam();
        Render<?> render = renderManager.getRender(slot.getShip().getId());
        if (render instanceof ShipRender shipRender) {
            ((WeaponSlotBeamRender) shipRender.getWeaponRender(slot.getId())).onDamage(event.raycast(), event.hitX(),
                    event.hitY());
            GarbageSpawner.beamHullDamage(event.hitX(), event.hitY(), wreck.getVelocity().x * 0.005f,
                    wreck.getVelocity().y * 0.005f);
        }
    }
}