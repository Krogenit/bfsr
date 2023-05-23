package net.bfsr.event.module.weapon.beam;

import org.dyn4j.collision.narrowphase.Raycast;

public record BeamDamageShipEvent(net.bfsr.component.weapon.WeaponSlotBeam slotBeam, net.bfsr.entity.ship.Ship ship, Raycast raycast, float hitX,
                                  float hitY) {}