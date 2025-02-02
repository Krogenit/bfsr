package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
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
        GameObject bulletGameObject = Client.get().getWorld().getEntityById(packet.getBulletId());
        GameObject shipGameObject = Client.get().getWorld().getEntityById(packet.getShipId());
        if (bulletGameObject instanceof Bullet bullet && shipGameObject instanceof Ship ship) {
            DamageType damageType = packet.getDamageType();
            if (damageType == DamageType.ARMOR) {
                GarbageSpawner.bulletArmorDamage(packet.getContactX(), packet.getContactY(), ship.getLinearVelocity().x,
                        ship.getLinearVelocity().y, packet.getNormalX(), packet.getNormalY());
            } else if (damageType == DamageType.HULL) {
                GarbageSpawner.bulletHullDamage(packet.getContactX(), packet.getContactY(), ship.getLinearVelocity().x,
                        ship.getLinearVelocity().y, packet.getNormalX(), packet.getNormalY());
            }

            Vector4f color = bullet.getGunData().getColor();
            WeaponEffects.spawnDirectedSpark(packet.getContactX(), packet.getContactY(), packet.getNormalX(),
                    packet.getNormalY(), bullet.getSizeX() * 1.5f, color.x, color.y, color.z,
                    1.0f - bullet.getLifeTime() / (float) bullet.getMaxLifeTime());
        }
    }
}