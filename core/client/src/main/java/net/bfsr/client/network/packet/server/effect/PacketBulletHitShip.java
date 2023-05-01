package net.bfsr.client.network.packet.server.effect;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.Shield;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.IOException;

public class PacketBulletHitShip implements PacketIn {
    private int bulletId, shipId;
    private float contactX, contactY;
    private float normalX, normalY;

    @Override
    public void read(ByteBuf data) throws IOException {
        bulletId = data.readInt();
        shipId = data.readInt();
        contactX = data.readFloat();
        contactY = data.readFloat();
        normalX = data.readFloat();
        normalY = data.readFloat();
    }

    @Override
    public void processOnClientSide() {
        GameObject bulletGameObject = Core.get().getWorld().getEntityById(bulletId);
        GameObject shipGameObject = Core.get().getWorld().getEntityById(shipId);
        if (bulletGameObject instanceof Bullet bullet && shipGameObject instanceof Ship ship) {
            Shield shield = ship.getShield();
            Vector2f bulletSize = bullet.getSize();
            if (shield == null || shield.getShield() <= 0) {
                Hull hull = ship.getHull();
                GarbageSpawner.bulletHullDamage(contactX, contactY, ship.getVelocity().x, ship.getVelocity().y, normalX, normalY,
                        () -> hull.getHull() / hull.getMaxHull() < 0.5f);
            }

            Vector4f color = Core.get().getRenderer().getRender(bulletId).getColor();
            WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, bulletSize.x * 1.5f, color.x, color.y, color.z, color.w);
        }
    }
}