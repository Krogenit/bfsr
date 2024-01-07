package net.bfsr.server.event.listener.module.weapon;

import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.event.module.weapon.BeamShotEvent;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import net.bfsr.network.packet.server.component.PacketRemoveWeaponSlot;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class WeaponEventListener {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();
    private final World world = ServerGameLogic.getInstance().getWorld();

    @Handler
    public void event(WeaponShotEvent event) {
        WeaponSlot weaponSlot = event.weaponSlot();
        Ship ship = weaponSlot.getShip();
        weaponSlot.createBullet(0, bullet -> networkSystem.sendTCPPacketToAllNearby(
                new PacketSpawnEntity(bullet.createSpawnData(), world.getTimestamp()), bullet.getPosition(),
                TrackingUtils.TRACKING_DISTANCE));
        networkSystem.sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), weaponSlot.getId()), ship.getPosition(),
                TrackingUtils.TRACKING_DISTANCE);
    }

    @Handler
    public void event(BeamShotEvent event) {
        WeaponSlotBeam weaponSlot = event.weaponSlot();
        Ship ship = weaponSlot.getShip();
        networkSystem.sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), weaponSlot.getId()), ship.getPosition(),
                TrackingUtils.TRACKING_DISTANCE);
    }

    @Handler
    public void event(WeaponSlotRemovedEvent event) {
        WeaponSlot weaponSlot = event.getWeaponSlot();
        Ship ship = weaponSlot.getShip();
        networkSystem.sendTCPPacketToAllNearby(new PacketRemoveWeaponSlot(ship.getId(), weaponSlot.getId()), ship.getPosition(),
                TrackingUtils.TRACKING_DISTANCE);
    }
}