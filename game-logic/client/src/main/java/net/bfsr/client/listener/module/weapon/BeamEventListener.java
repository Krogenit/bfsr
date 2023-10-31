package net.bfsr.client.listener.module.weapon;

import net.bfsr.client.Core;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.component.WeaponSlotBeamRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageShipEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class BeamEventListener {
    private final RenderManager renderManager = Core.get().getRenderManager();

    @Handler
    public void event(BeamDamageShipEvent event) {
        WeaponSlotBeam slot = event.slotBeam();
        Ship ship = event.ship();
        ShipRender render = renderManager.getRender(slot.getShip().getId());
        ((WeaponSlotBeamRender) render.getWeaponRender(slot.getId())).onDamage(event.raycast(), event.hitX(), event.hitY());

        Shield shield = ship.getModules().getShield();
        if (shield == null || shield.getShield() <= 0) {
            Hull hull = ship.getModules().getHull();
            GarbageSpawner.beamHullDamage(event.hitX(), event.hitY(), ship.getVelocity().x * 0.005f, ship.getVelocity().y * 0.005f,
                    () -> hull.getValue() / hull.getMaxValue() < 0.5f);
        }
    }

    @Handler
    public void event(BeamDamageWreckEvent event) {
        Wreck wreck = event.wreck();
        WeaponSlotBeam slot = event.slotBeam();
        ShipRender render = renderManager.getRender(slot.getShip().getId());
        ((WeaponSlotBeamRender) render.getWeaponRender(slot.getId())).onDamage(event.raycast(), event.hitX(), event.hitY());
        GarbageSpawner.beamHullDamage(event.hitX(), event.hitY(), wreck.getVelocity().x * 0.005f, wreck.getVelocity().y * 0.005f);
    }
}