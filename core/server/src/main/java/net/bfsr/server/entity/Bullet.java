package net.bfsr.server.entity;

import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.MainServer;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.packet.server.PacketSpawnBullet;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;

import java.util.Random;

public abstract class Bullet extends BulletCommon {
    protected Bullet(WorldServer world, int id, float bulletSpeed, float x, float y, float scaleX, float scaleY, ShipCommon ship, float r, float g, float b, float a,
                     float alphaReducer, BulletDamage damage) {
        super(world, id, bulletSpeed, x, y, ship.getSin(), ship.getCos(), scaleX, scaleY, ship, r, g, b, a, alphaReducer, damage);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnBullet(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    protected void destroyBullet(CollisionObject destroyer, Contact contact, Vector2 normal) {
        if (destroyer != null) {
            if (destroyer instanceof ShipCommon s) {
                ShieldCommon shield = s.getShield();
                if (shield == null || shield.getShield() <= 0) {
                    Hull hull = s.getHull();
                    Vector2 pos1 = contact.getPoint();
                    float velocityX = destroyer.getVelocity().x * 0.005f;
                    float velocityY = destroyer.getVelocity().y * 0.005f;
                    Random rand = world.getRand();
                    if (hull.getHull() / hull.getMaxHull() < 0.25f && rand.nextInt(2) == 0) {
                        RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, angleToVelocity);
                        WreckSpawner.spawnDamageDebris((WorldServer) world, rand.nextInt(2), (float) pos1.x, (float) pos1.y,
                                velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.75f);
                    }
                }
            }
        }
    }
}
