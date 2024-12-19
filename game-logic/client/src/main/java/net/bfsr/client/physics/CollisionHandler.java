package net.bfsr.client.physics;

import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.CommonCollisionHandler;
import net.bfsr.physics.correction.DynamicCorrectionHandler;
import net.bfsr.world.World;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class CollisionHandler extends CommonCollisionHandler {
    private final Vector2f angleToVelocity = new Vector2f();
    private final BeamDamageShipShieldEvent beamDamageShipShieldEvent = new BeamDamageShipShieldEvent();
    private final BeamDamageShipArmorEvent beamDamageShipArmorEvent = new BeamDamageShipArmorEvent();
    private final BeamDamageShipHullEvent beamDamageShipHullEvent = new BeamDamageShipHullEvent();

    public CollisionHandler(EventBus eventBus) {
        super(eventBus);
        eventBus.optimizeEvent(beamDamageShipShieldEvent);
        eventBus.optimizeEvent(beamDamageShipArmorEvent);
        eventBus.optimizeEvent(beamDamageShipHullEvent);
    }

    @Override
    public void bulletShip(Bullet bullet, Ship ship, Fixture bulletFixture, Fixture shipFixture, float contactX,
                           float contactY, float normalX, float normalY) {
        if (bullet.getLastCollidedRigidBody() == ship) return;

        super.bulletShip(bullet, ship, bulletFixture, shipFixture, contactX, contactY, normalX, normalY);

        Vector4f color = bullet.getGunData().getColor();
        float colorAlpha = (1.0f - bullet.getLifeTime() / (float) bullet.getMaxLifeTime()) * 1.5f;
        damageShip(ship, contactX, contactY, () -> {
            bullet.reflect(normalX, normalY);
            WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, bullet.getSizeX() * 1.5f, color.x, color.y,
                    color.z, colorAlpha);
        }, () -> {
            WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, bullet.getSizeX() * 1.5f, color.x, color.y,
                    color.z, colorAlpha);
            GarbageSpawner.bulletArmorDamage(contactX, contactY, ship.getLinearVelocity().x, ship.getLinearVelocity().y, normalX, normalY);
            bullet.setDead();
        }, () -> {
            WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, bullet.getSizeX() * 1.5f, color.x, color.y,
                    color.z, colorAlpha);
            GarbageSpawner.bulletHullDamage(contactX, contactY, ship.getLinearVelocity().x, ship.getLinearVelocity().y, normalX, normalY);
            bullet.setDead();
        });
    }

    @Override
    public void shipShip(Ship ship1, Ship ship2, Fixture ship1Fixture, Fixture ship2Fixture, float contactX,
                         float contactY, float normalX, float normalY) {
        Vector2 linearVelocity1 = ship1.getLinearVelocity();
        Vector2 linearVelocity2 = ship2.getLinearVelocity();
        float dx = linearVelocity2.x - linearVelocity1.x;
        float dy = linearVelocity2.y - linearVelocity1.y;
        float impactPower = (Math.sqrt(dx * dx + dy * dy)) * (ship1.getBody().getMass() / ship2.getBody().getMass());

        impactPower /= 10.0f;

        if (impactPower > 0.25f) {
            if (ship1.getCollisionTimer() <= 0) {
                ship1.setCollisionTimer(ship1.getWorld().convertToTicks(0.5f));
                ship1.setLastAttacker(ship2);
                damageShipByCollision(ship1, contactX, contactY, -normalX, -normalY);
            }

            if (ship2.getCollisionTimer() <= 0) {
                ship2.setCollisionTimer(ship2.getWorld().convertToTicks(0.5f));
                ship2.setLastAttacker(ship1);
                damageShipByCollision(ship2, contactX, contactY, normalX, normalY);
            }

            if (ship1.isControlledByPlayer()) {
                setDynamicCorrection(ship2);
            } else if (ship2.isControlledByPlayer()) {
                setDynamicCorrection(ship1);
            }
        }
    }

    @Override
    public void shipWreck(Ship ship, Wreck wreck, Fixture shipFixture, Fixture wreckFixture, float contactX,
                          float contactY, float normalX, float normalY) {
        if (ship.getCollisionTimer() <= 0) {
            ship.setCollisionTimer(ship.getWorld().convertToTicks(0.5f));
            Shield shield = ship.getModules().getShield();
            if (shield != null && isShieldAlive(shield)) {
                Vector4f color = ship.getShipData().getEffectsColor();
                WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 4.5f, color.x, color.y, color.z, color.w);
            } else {
                WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        if (ship.isControlledByPlayer()) {
            setDynamicCorrection(wreck);
        }
    }

    @Override
    public void weaponSlotBeamShip(WeaponSlotBeam weaponSlot, Ship ship, Fixture fixture, float contactX, float contactY,
                                   float normalX, float normalY) {
        EventBus weaponSlotEventBus = weaponSlot.getWeaponSlotEventBus();
        damageShip(ship, contactX, contactY, () -> weaponSlotEventBus.publishOptimized(beamDamageShipShieldEvent.set(weaponSlot,
                        ship, contactX, contactY, normalX, normalY)),
                () -> weaponSlotEventBus.publishOptimized(beamDamageShipArmorEvent.set(weaponSlot, ship, contactX, contactY,
                        normalX, normalY)),
                () -> weaponSlotEventBus.publishOptimized(beamDamageShipHullEvent.set(weaponSlot, ship, contactX, contactY,
                        normalX, normalY)));
    }

    private void damageShip(Ship ship, float contactX, float contactY, Runnable onShieldDamageRunnable,
                            Runnable onArmorDamageRunnable, Runnable onHullDamageRunnable) {
        Modules modules = ship.getModules();
        Shield shield = modules.getShield();
        if (shield != null && isShieldAlive(shield)) {
            onShieldDamageRunnable.run();
            return;
        }

        ArmorPlate armorPlate = modules.getArmor().getCell(contactX, contactY, ship);
        if (armorPlate != null && armorPlate.getValue() > 0) {
            onArmorDamageRunnable.run();
        } else {
            onHullDamageRunnable.run();
        }
    }

    private void damageShipByCollision(Ship ship, float contactX, float contactY, float normalX, float normalY) {
        Modules modules = ship.getModules();
        Shield shield = modules.getShield();
        if (shield != null && isShieldAlive(shield)) {
            Vector4f color = ship.getShipData().getEffectsColor();
            WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 4.5f, color.x, color.y, color.z, color.w);
            return;
        }

        World world = ship.getWorld();
        Random rand = world.getRand();
        Vector2 velocity = ship.getLinearVelocity();
        WeaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 3.75f, 1.0f,
                1.0f, 1.0f, 1.0f);
        RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.15f, angleToVelocity);
        GarbageSpawner.smallGarbage(rand.nextInt(4), contactX, contactY,
                velocity.x * 0.25f + angleToVelocity.x, velocity.y * 0.25f + angleToVelocity.y, 2.0f * rand.nextFloat());
    }

    private void setDynamicCorrection(RigidBody rigidBody) {
        rigidBody.setCorrectionHandler(
                new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.1f), rigidBody.getCorrectionHandler()));
    }

    private boolean isShieldAlive(Shield shield) {
        if (shield.isDead()) return false;

        return shield.getShieldHp() > 0;
    }
}