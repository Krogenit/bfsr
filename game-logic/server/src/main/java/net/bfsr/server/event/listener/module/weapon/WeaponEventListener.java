package net.bfsr.server.event.listener.module.weapon;

import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.weapon.BeamShotEvent;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.util.TrackingUtils;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class WeaponEventListener {
    @Handler
    public void event(WeaponShotEvent event) {
        WeaponSlot weaponSlot = event.weaponSlot();
        Ship ship = weaponSlot.getShip();
        weaponSlot.createBullet();
        ServerGameLogic.getNetwork().sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), weaponSlot.getId()), ship.getPosition(),
                TrackingUtils.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(BeamShotEvent event) {
        WeaponSlotBeam weaponSlot = event.weaponSlot();
        Ship ship = weaponSlot.getShip();
        ServerGameLogic.getNetwork().sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), weaponSlot.getId()), ship.getPosition(),
                TrackingUtils.PACKET_SPAWN_DISTANCE);
    }
}