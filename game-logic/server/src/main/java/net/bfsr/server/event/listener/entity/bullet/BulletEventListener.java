package net.bfsr.server.event.listener.entity.bullet;

import net.bfsr.component.hull.Hull;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDeathEvent;
import net.bfsr.event.entity.bullet.BulletHitShipEvent;
import net.bfsr.event.entity.bullet.BulletReflectEvent;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.core.Server;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.server.effect.PacketBulletHitShip;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.world.WorldServer;
import net.bfsr.world.World;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import java.util.Random;

@Listener(references = References.Strong)
public class BulletEventListener {
    @Handler
    public void event(BulletReflectEvent event) {
        Bullet bullet = event.bullet();
        Server.getNetwork().sendUDPPacketToAllNearby(new PacketObjectPosition(bullet), bullet.getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
    }

    @Handler
    public void event(BulletHitShipEvent event) {
        Bullet bullet = event.bullet();
        Server.getNetwork().sendUDPPacketToAllNearby(new PacketBulletHitShip(bullet, event.ship(), event.contactX(), event.contactY(),
                event.normalX(), event.normalY()), bullet.getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
    }

    @Handler
    public void event(BulletDamageShipHullEvent event) {
        Ship ship = event.ship();
        Hull hull = ship.getHull();
        World world = ship.getWorld();
        Random rand = world.getRand();
        if (hull.getHull() / hull.getMaxHull() < 0.25f && rand.nextInt(2) == 0) {
            RotationHelper.angleToVelocity(net.bfsr.common.math.MathUtils.TWO_PI * rand.nextFloat(), 1.5f, MathUtils.ANGLE_TO_VELOCITY);
            float velocityX = ship.getVelocity().x * 0.005f;
            float velocityY = ship.getVelocity().y * 0.005f;
            WreckSpawner.spawnDamageDebris(((WorldServer) world), rand.nextInt(2), event.contactX(), event.contactY(),
                    velocityX + MathUtils.ANGLE_TO_VELOCITY.x, velocityY + MathUtils.ANGLE_TO_VELOCITY.y, 0.75f);
        }
    }

    @Handler
    public void event(BulletDeathEvent event) {
        Bullet bullet = event.bullet();
        Server.getNetwork().sendTCPPacketToAll(new PacketRemoveObject(bullet));
    }
}