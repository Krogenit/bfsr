package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.client.renderer.Render;
import net.bfsr.damage.DamageType;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.effect.PacketBulletHitShip;
import org.joml.Vector4f;

import java.net.InetSocketAddress;

public class PacketBulletHitShipHandler extends PacketHandler<PacketBulletHitShip, NetworkSystem> {
    @Override
    public void handle(PacketBulletHitShip packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject bulletGameObject = Core.get().getWorld().getEntityById(packet.getBulletId());
        GameObject shipGameObject = Core.get().getWorld().getEntityById(packet.getShipId());
        if (bulletGameObject instanceof Bullet bullet && shipGameObject instanceof Ship ship) {
            DamageType damageType = packet.getDamageType();
            if (damageType == DamageType.ARMOR) {
                GarbageSpawner.bulletArmorDamage(packet.getContactX(), packet.getContactY(), ship.getVelocity().x,
                        ship.getVelocity().y, packet.getNormalX(), packet.getNormalY());
            } else if (damageType == DamageType.HULL) {
                GarbageSpawner.bulletHullDamage(packet.getContactX(), packet.getContactY(), ship.getVelocity().x,
                        ship.getVelocity().y, packet.getNormalX(), packet.getNormalY());
            }

            Render<?> render = Core.get().getRenderManager().getRender(packet.getBulletId());
            if (render != null) {
                Vector4f color = render.getColor();
                WeaponEffects.spawnDirectedSpark(packet.getContactX(), packet.getContactY(), packet.getNormalX(),
                        packet.getNormalY(),
                        bullet.getSize().x * 1.5f, color.x, color.y, color.z, color.w);
            }
        }
    }
}