package net.bfsr.server.event.listener.entity.bullet;

import net.bfsr.damage.DamageType;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.bullet.BulletDamageShipArmorEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipShieldEvent;
import net.bfsr.event.entity.bullet.BulletReflectEvent;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.effect.PacketBulletHitShip;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import java.util.Random;

import static net.bfsr.math.RigidBodyUtils.ANGLE_TO_VELOCITY;

@Listener(references = References.Strong)
public class BulletEventListener {
    private final NetworkSystem network = ServerGameLogic.getNetwork();

    @Handler
    public void event(BulletReflectEvent event) {
        Bullet bullet = event.bullet();
        network.sendUDPPacketToAllNearby(new PacketObjectPosition(bullet), bullet.getPosition(), TrackingUtils.TRACKING_DISTANCE);
    }

    @Handler
    public void event(BulletDamageShipShieldEvent event) {
        sendHitPacket(event.getBullet(), event.getShip(), event.getContactX(), event.getContactY(), event.getNormalX(),
                event.getNormalY(), DamageType.SHIELD);
    }

    @Handler
    public void event(BulletDamageShipArmorEvent event) {
        sendHitPacket(event.getBullet(), event.getShip(), event.getContactX(), event.getContactY(), event.getNormalX(),
                event.getNormalY(), DamageType.ARMOR);
    }

    @Handler
    public void event(BulletDamageShipHullEvent event) {
        Ship ship = event.getShip();
        World world = ship.getWorld();
        Random rand = world.getRand();
        if (rand.nextInt(2) == 0) {
            RotationHelper.angleToVelocity(net.bfsr.engine.math.MathUtils.TWO_PI * rand.nextFloat(), 1.5f, ANGLE_TO_VELOCITY);
            float velocityX = ship.getVelocity().x * 0.005f;
            float velocityY = ship.getVelocity().y * 0.005f;
            WreckSpawner.spawnDamageDebris(world, rand.nextInt(2), event.getContactX(), event.getContactY(),
                    velocityX + ANGLE_TO_VELOCITY.x, velocityY + ANGLE_TO_VELOCITY.y, 0.75f);
        }

        sendHitPacket(event.getBullet(), event.getShip(), event.getContactX(), event.getContactY(), event.getNormalX(),
                event.getNormalY(), DamageType.HULL);
    }

    private void sendHitPacket(Bullet bullet, Ship ship, float contactX, float contactY, float normalX, float normalY,
                               DamageType damageType) {
        network.sendUDPPacketToAllNearby(new PacketBulletHitShip(bullet, ship, contactX, contactY, normalX, normalY, damageType),
                bullet.getPosition(), TrackingUtils.TRACKING_DISTANCE);
    }
}