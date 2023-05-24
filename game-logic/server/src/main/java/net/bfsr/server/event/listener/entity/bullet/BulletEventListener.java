package net.bfsr.server.event.listener.entity.bullet;

import net.bfsr.component.hull.Hull;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.bullet.*;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.effect.PacketBulletHitShip;
import net.bfsr.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.network.packet.server.entity.bullet.PacketSpawnBullet;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import java.util.Random;

@Listener(references = References.Strong)
public class BulletEventListener {
    @Handler
    public void event(BulletAddToWorldEvent event) {
        Bullet bullet = event.bullet();
        ServerGameLogic.getNetwork().sendUDPPacketToAllNearby(new PacketSpawnBullet(bullet), bullet.getPosition(), TrackingUtils.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(BulletReflectEvent event) {
        Bullet bullet = event.bullet();
        ServerGameLogic.getNetwork().sendUDPPacketToAllNearby(new PacketObjectPosition(bullet), bullet.getPosition(), TrackingUtils.PACKET_UPDATE_DISTANCE);
    }

    @Handler
    public void event(BulletHitShipEvent event) {
        Bullet bullet = event.bullet();
        ServerGameLogic.getNetwork().sendUDPPacketToAllNearby(new PacketBulletHitShip(bullet, event.ship(), event.contactX(), event.contactY(),
                event.normalX(), event.normalY()), bullet.getPosition(), TrackingUtils.PACKET_UPDATE_DISTANCE);
    }

    @Handler
    public void event(BulletDamageShipHullEvent event) {
        Ship ship = event.ship();
        Hull hull = ship.getHull();
        World world = ship.getWorld();
        Random rand = world.getRand();
        if (hull.getHull() / hull.getMaxHull() < 0.25f && rand.nextInt(2) == 0) {
            RotationHelper.angleToVelocity(net.bfsr.engine.math.MathUtils.TWO_PI * rand.nextFloat(), 1.5f, RigidBodyUtils.ANGLE_TO_VELOCITY);
            float velocityX = ship.getVelocity().x * 0.005f;
            float velocityY = ship.getVelocity().y * 0.005f;
            WreckSpawner.spawnDamageDebris(world, rand.nextInt(2), event.contactX(), event.contactY(),
                    velocityX + RigidBodyUtils.ANGLE_TO_VELOCITY.x, velocityY + RigidBodyUtils.ANGLE_TO_VELOCITY.y, 0.75f);
        }
    }

    @Handler
    public void event(BulletDeathEvent event) {
        Bullet bullet = event.bullet();
        ServerGameLogic.getNetwork().sendTCPPacketToAll(new PacketRemoveObject(bullet));
    }
}