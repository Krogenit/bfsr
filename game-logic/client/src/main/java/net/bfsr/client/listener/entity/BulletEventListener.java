package net.bfsr.client.listener.entity;

import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.bullet.BulletDamageShipArmorEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipShieldEvent;
import org.joml.Vector4f;

public class BulletEventListener {
    @EventHandler
    public EventListener<BulletDamageShipShieldEvent> bulletDamageShipShieldEvent() {
        return event -> {
            Bullet bullet = event.getBullet();
            Vector4f color = bullet.getConfigData().getColor();
            WeaponEffects.spawnDirectedSpark(event.getContactX(), event.getContactY(), event.getNormalX(),
                    event.getNormalY(), bullet.getSize().x * 1.5f, color.x, color.y, color.z,
                    (1.0f - bullet.getLifeTime() / bullet.getMaxLifeTime()) * 1.5f);
        };
    }

    @EventHandler
    public EventListener<BulletDamageShipArmorEvent> bulletDamageShipArmorEvent() {
        return event -> {
            Bullet bullet = event.getBullet();
            Vector4f color = bullet.getConfigData().getColor();
            WeaponEffects.spawnDirectedSpark(event.getContactX(), event.getContactY(), event.getNormalX(),
                    event.getNormalY(), bullet.getSize().x * 1.5f, color.x, color.y, color.z,
                    (1.0f - bullet.getLifeTime() / bullet.getMaxLifeTime()) * 1.5f);

            Ship ship = event.getShip();
            GarbageSpawner.bulletArmorDamage(event.getContactX(), event.getContactY(), ship.getVelocity().x,
                    ship.getVelocity().y, event.getNormalX(), event.getNormalY());
        };
    }

    @EventHandler
    public EventListener<BulletDamageShipHullEvent> bulletDamageShipHullEvent() {
        return event -> {
            Bullet bullet = event.getBullet();
            Vector4f color = bullet.getConfigData().getColor();
            WeaponEffects.spawnDirectedSpark(event.getContactX(), event.getContactY(), event.getNormalX(),
                    event.getNormalY(), bullet.getSize().x * 1.5f, color.x, color.y, color.z,
                    (1.0f - bullet.getLifeTime() / bullet.getMaxLifeTime()) * 1.5f);

            Ship ship = event.getShip();
            GarbageSpawner.bulletHullDamage(event.getContactX(), event.getContactY(), ship.getVelocity().x,
                    ship.getVelocity().y, event.getNormalX(), event.getNormalY());
        };
    }
}