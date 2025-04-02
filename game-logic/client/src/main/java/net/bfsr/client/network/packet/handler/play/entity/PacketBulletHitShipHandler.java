package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.damage.DamageType;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.GameObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.effect.PacketBulletHitShip;
import org.joml.Vector4f;

import java.net.InetSocketAddress;

public class PacketBulletHitShipHandler extends PacketHandler<PacketBulletHitShip, NetworkSystem> {
    private final Client client = Client.get();
    private final GarbageSpawner garbageSpawner = client.getParticleEffects().getGarbageSpawner();
    private final WeaponEffects weaponEffects = client.getParticleEffects().getWeaponEffects();

    @Override
    public void handle(PacketBulletHitShip packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        World world = client.getWorld();
        GameObject bulletGameObject = world.getEntityById(packet.getBulletId());
        GameObject shipGameObject = world.getEntityById(packet.getShipId());
        if (bulletGameObject instanceof Bullet bullet && shipGameObject instanceof Ship ship) {
            DamageType damageType = packet.getDamageType();
            if (damageType == DamageType.ARMOR) {
                garbageSpawner.bulletArmorDamage(packet.getContactX(), packet.getContactY(), ship.getLinearVelocity().x,
                        ship.getLinearVelocity().y, packet.getNormalX(), packet.getNormalY());
            } else if (damageType == DamageType.HULL) {
                garbageSpawner.bulletHullDamage(packet.getContactX(), packet.getContactY(), ship.getLinearVelocity().x,
                        ship.getLinearVelocity().y, packet.getNormalX(), packet.getNormalY());
            }

            Vector4f color = bullet.getGunData().getColor();
            weaponEffects.spawnDirectedSpark(packet.getContactX(), packet.getContactY(), packet.getNormalX(),
                    packet.getNormalY(), bullet.getSizeX() * 1.5f, color.x, color.y, color.z,
                    1.0f - bullet.getLifeTime() / (float) bullet.getMaxLifeTime());
        }
    }
}