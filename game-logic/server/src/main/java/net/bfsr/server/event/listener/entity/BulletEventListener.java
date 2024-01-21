package net.bfsr.server.event.listener.entity;

import net.bfsr.damage.DamageType;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.bullet.BulletDamageShipArmorEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipShieldEvent;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.server.effect.PacketBulletHitShip;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.util.Random;

public class BulletEventListener {
    private final NetworkSystem network = ServerGameLogic.getNetwork();
    private final Vector2f angleToVelocity = new Vector2f();

    @EventHandler
    public EventListener<BulletDamageShipShieldEvent> bulletDamageShipShieldEvent() {
        return event -> sendHitPacket(event.getBullet(), event.getShip(), event.getContactX(), event.getContactY(),
                event.getNormalX(), event.getNormalY(), DamageType.SHIELD);
    }

    @EventHandler
    public EventListener<BulletDamageShipArmorEvent> bulletDamageShipArmorEvent() {
        return event -> sendHitPacket(event.getBullet(), event.getShip(), event.getContactX(), event.getContactY(),
                event.getNormalX(), event.getNormalY(), DamageType.ARMOR);
    }

    @EventHandler
    public EventListener<BulletDamageShipHullEvent> event() {
        return event -> {
            Ship ship = event.getShip();
            World world = ship.getWorld();
            Random rand = world.getRand();
            if (rand.nextInt(2) == 0) {
                RotationHelper.angleToVelocity(net.bfsr.engine.math.MathUtils.TWO_PI * rand.nextFloat(), 1.5f, angleToVelocity);
                float velocityX = ship.getVelocity().x * 0.005f;
                float velocityY = ship.getVelocity().y * 0.005f;
                WreckSpawner.spawnDamageDebris(world, rand.nextInt(2), event.getContactX(), event.getContactY(),
                        velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.75f);
            }

            sendHitPacket(event.getBullet(), event.getShip(), event.getContactX(), event.getContactY(), event.getNormalX(),
                    event.getNormalY(), DamageType.HULL);
        };
    }

    private void sendHitPacket(Bullet bullet, Ship ship, float contactX, float contactY, float normalX, float normalY,
                               DamageType damageType) {
        network.sendUDPPacketToAllNearby(new PacketBulletHitShip(bullet, ship, contactX, contactY, normalX, normalY, damageType),
                bullet.getPosition(), TrackingUtils.TRACKING_DISTANCE);
    }
}