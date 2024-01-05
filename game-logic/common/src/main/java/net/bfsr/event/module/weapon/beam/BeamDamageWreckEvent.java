package net.bfsr.event.module.weapon.beam;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import org.dyn4j.collision.narrowphase.Raycast;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class BeamDamageWreckEvent extends Event {
    private final WeaponSlotBeam slotBeam;
    private final Wreck wreck;
    private final Raycast raycast;
    private final float hitX;
    private final float hitY;
}