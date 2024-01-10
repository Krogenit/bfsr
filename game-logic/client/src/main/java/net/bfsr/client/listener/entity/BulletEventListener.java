package net.bfsr.client.listener.entity;

import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.bullet.BulletDamageShipArmorEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipShieldEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.joml.Vector4f;

@Listener(references = References.Strong)
public class BulletEventListener {
    @Handler
    public void event(BulletDamageShipShieldEvent event) {
        Bullet bullet = event.getBullet();
        Vector4f color = bullet.getConfigData().getColor();
        WeaponEffects.spawnDirectedSpark(event.getContactX(), event.getContactY(), event.getNormalX(),
                event.getNormalY(), bullet.getSize().x * 1.5f, color.x, color.y, color.z,
                (1.0f - bullet.getLifeTime() / bullet.getMaxLifeTime()) * 1.5f);
    }

    @Handler
    public void event(BulletDamageShipArmorEvent event) {
        Bullet bullet = event.getBullet();
        Vector4f color = bullet.getConfigData().getColor();
        WeaponEffects.spawnDirectedSpark(event.getContactX(), event.getContactY(), event.getNormalX(),
                event.getNormalY(), bullet.getSize().x * 1.5f, color.x, color.y, color.z,
                (1.0f - bullet.getLifeTime() / bullet.getMaxLifeTime()) * 1.5f);

        Ship ship = event.getShip();
        GarbageSpawner.bulletArmorDamage(event.getContactX(), event.getContactY(), ship.getVelocity().x,
                ship.getVelocity().y, event.getNormalX(), event.getNormalY());
    }

    @Handler
    public void event(BulletDamageShipHullEvent event) {
        Bullet bullet = event.getBullet();
        Vector4f color = bullet.getConfigData().getColor();
        WeaponEffects.spawnDirectedSpark(event.getContactX(), event.getContactY(), event.getNormalX(),
                event.getNormalY(), bullet.getSize().x * 1.5f, color.x, color.y, color.z,
                (1.0f - bullet.getLifeTime() / bullet.getMaxLifeTime()) * 1.5f);

        Ship ship = event.getShip();
        GarbageSpawner.bulletHullDamage(event.getContactX(), event.getContactY(), ship.getVelocity().x,
                ship.getVelocity().y, event.getNormalX(), event.getNormalY());
    }
}