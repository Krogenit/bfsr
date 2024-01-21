package net.bfsr.server.event.listener.module.weapon;

import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import net.bfsr.network.packet.server.component.PacketRemoveWeaponSlot;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.player.Player;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;

public class WeaponEventListener {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();
    private final World world = ServerGameLogic.getInstance().getWorld();

    @EventHandler
    public EventListener<WeaponShotEvent> weaponShotEvent() {
        return event -> {
            WeaponSlot weaponSlot = event.weaponSlot();
            Ship ship = weaponSlot.getShip();
            if (ship.isControlledByPlayer()) {
                Player player = ServerGameLogic.getInstance().getPlayerManager().getPlayerControllingShip(ship);
                weaponSlot.createBullet((float) (Engine.getClientRenderDelayInMills() + player.getNetworkHandler().getPing()),
                        bullet -> networkSystem.sendUDPPacketToAllNearbyExcept(new PacketSpawnEntity(bullet.createSpawnData(),
                                world.getTimestamp()), bullet.getPosition(), TrackingUtils.TRACKING_DISTANCE, player));
                networkSystem.sendUDPPacketToAllNearbyExcept(new PacketWeaponShoot(ship.getId(), weaponSlot.getId(),
                        world.getTimestamp()), ship.getPosition(), TrackingUtils.TRACKING_DISTANCE, player);
            } else {
                weaponSlot.createBullet(0, bullet -> networkSystem.sendTCPPacketToAllNearby(new PacketSpawnEntity(
                        bullet.createSpawnData(), world.getTimestamp()), bullet.getPosition(), TrackingUtils.TRACKING_DISTANCE));
                networkSystem.sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), weaponSlot.getId(),
                        world.getTimestamp()), ship.getPosition(), TrackingUtils.TRACKING_DISTANCE);
            }
        };
    }

    @EventHandler
    public EventListener<WeaponSlotRemovedEvent> weaponSlotRemovedEvent() {
        return event -> {
            WeaponSlot weaponSlot = event.getWeaponSlot();
            Ship ship = weaponSlot.getShip();
            networkSystem.sendTCPPacketToAllNearby(new PacketRemoveWeaponSlot(ship.getId(), weaponSlot.getId(),
                    world.getTimestamp()), ship.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }
}