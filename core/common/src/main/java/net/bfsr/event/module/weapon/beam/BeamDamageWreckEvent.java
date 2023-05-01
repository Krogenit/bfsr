package net.bfsr.event.module.weapon.beam;

import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import org.dyn4j.collision.narrowphase.Raycast;

public record BeamDamageWreckEvent(WeaponSlotBeam slotBeam, Wreck wreck, Raycast raycast, float hitX, float hitY) {}