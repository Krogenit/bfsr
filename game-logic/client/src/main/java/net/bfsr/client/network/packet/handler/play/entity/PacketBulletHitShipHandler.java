package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.Shield;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.effect.PacketBulletHitShip;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.net.InetSocketAddress;

public class PacketBulletHitShipHandler extends PacketHandler<PacketBulletHitShip, NetworkSystem> {
    @Override
    public void handle(PacketBulletHitShip packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject bulletGameObject = Core.get().getWorld().getEntityById(packet.getBulletId());
        GameObject shipGameObject = Core.get().getWorld().getEntityById(packet.getShipId());
        if (bulletGameObject instanceof Bullet bullet && shipGameObject instanceof Ship ship) {
            Shield shield = ship.getShield();
            Vector2f bulletSize = bullet.getSize();
            if (shield == null || shield.getShield() <= 0) {
                Hull hull = ship.getHull();
                GarbageSpawner.bulletHullDamage(packet.getContactX(), packet.getContactY(), ship.getVelocity().x, ship.getVelocity().y,
                        packet.getNormalX(), packet.getNormalY(), () -> hull.getHull() / hull.getMaxHull() < 0.5f);
            }

            Vector4f color = Core.get().getRenderManager().getRender(packet.getBulletId()).getColor();
            WeaponEffects.spawnDirectedSpark(packet.getContactX(), packet.getContactY(), packet.getNormalX(), packet.getNormalY(),
                    bulletSize.x * 1.5f, color.x, color.y, color.z, color.w);
        }
    }
}