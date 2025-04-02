package net.bfsr.event.module.weapon.beam;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;

@Getter
@Setter
@Accessors(fluent = true)
public class BeamDamageHullEvent extends Event {
    private WeaponSlotBeam slot;
    private RigidBody rigidBody;
    private float hitX, hitY;
    private float normalX, normalY;

    public BeamDamageHullEvent set(WeaponSlotBeam slot, RigidBody rigidBody, float hitX, float hitY, float normalX, float normalY) {
        this.slot = slot;
        this.rigidBody = rigidBody;
        this.hitX = hitX;
        this.hitY = hitY;
        this.normalX = normalX;
        this.normalY = normalY;
        return this;
    }
}