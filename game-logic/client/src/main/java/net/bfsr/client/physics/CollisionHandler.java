package net.bfsr.client.physics;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.client.Client;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.engine.Engine;
import net.bfsr.engine.entity.RigidBody;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.physics.correction.DynamicCorrectionHandler;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.physics.collision.CommonCollisionHandler;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class CollisionHandler extends CommonCollisionHandler {
    private final Vector2f angleToVelocity = new Vector2f();
    private final BeamDamageShipShieldEvent beamDamageShipShieldEvent = new BeamDamageShipShieldEvent();
    private final BeamDamageShipArmorEvent beamDamageShipArmorEvent = new BeamDamageShipArmorEvent();
    private final BeamDamageHullEvent beamDamageHullEvent = new BeamDamageHullEvent();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final WeaponEffects weaponEffects;
    private final GarbageSpawner garbageSpawner;

    public CollisionHandler(Client client) {
        super(client.getEventBus());
        eventBus.optimizeEvent(beamDamageShipShieldEvent);
        eventBus.optimizeEvent(beamDamageShipArmorEvent);
        eventBus.optimizeEvent(beamDamageHullEvent);
        weaponEffects = client.getParticleEffects().getWeaponEffects();
        garbageSpawner = client.getParticleEffects().getGarbageSpawner();
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
            weaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, bullet.getSizeX() * 1.5f, color.x, color.y,
                    color.z, colorAlpha);
        }, () -> {
            weaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, bullet.getSizeX() * 1.5f, color.x, color.y,
                    color.z, colorAlpha);
            garbageSpawner.bulletArmorDamage(contactX, contactY, ship.getLinearVelocity().x, ship.getLinearVelocity().y, normalX, normalY);
            bullet.setDead();
        }, () -> {
            weaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, bullet.getSizeX() * 1.5f, color.x, color.y,
                    color.z, colorAlpha);
            garbageSpawner.bulletHullDamage(contactX, contactY, ship.getLinearVelocity().x, ship.getLinearVelocity().y, normalX, normalY);
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
                ship1.setCollisionTimer(Engine.convertToTicks(0.5f));
                ship1.setLastAttacker(ship2);
                damageShipByCollision(ship1, contactX, contactY, -normalX, -normalY);
            }

            if (ship2.getCollisionTimer() <= 0) {
                ship2.setCollisionTimer(Engine.convertToTicks(0.5f));
                ship2.setLastAttacker(ship1);
                damageShipByCollision(ship2, contactX, contactY, normalX, normalY);
            }

            if (ship1.isControlledByPlayer()) {
                setDynamicCorrection(ship1);
                setDynamicCorrection(ship2);
            } else if (ship2.isControlledByPlayer()) {
                setDynamicCorrection(ship2);
                setDynamicCorrection(ship1);
            }
        }
    }

    @Override
    public void shipWreck(Ship ship, Wreck wreck, Fixture shipFixture, Fixture wreckFixture, float contactX,
                          float contactY, float normalX, float normalY) {
        if (ship.getCollisionTimer() <= 0) {
            ship.setCollisionTimer(Engine.convertToTicks(0.5f));
            Shield shield = ship.getModules().getShield();
            if (shield != null && isShieldAlive(shield)) {
                Vector4f color = ship.getConfigData().getEffectsColor();
                weaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 4.5f, color.x, color.y, color.z, color.w);
            } else {
                weaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        if (ship.isControlledByPlayer()) {
            setDynamicCorrection(ship);
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
                () -> weaponSlotEventBus.publishOptimized(beamDamageHullEvent.set(weaponSlot, ship, contactX, contactY,
                        normalX, normalY)));
    }

    @Override
    public void weaponSlotBeamShipWreck(WeaponSlotBeam weaponSlot, ShipWreck wreck, Fixture fixture, float contactX, float contactY,
                                        float normalX, float normalY) {
        weaponSlot.getWeaponSlotEventBus().publishOptimized(beamDamageHullEvent.set(weaponSlot, wreck, contactX, contactY,
                normalX, normalY));
    }

    private void damageShip(Ship ship, float contactX, float contactY, Runnable onShieldDamageRunnable,
                            Runnable onArmorDamageRunnable, Runnable onHullDamageRunnable) {
        Modules modules = ship.getModules();
        Shield shield = modules.getShield();
        if (shield != null && isShieldAlive(shield)) {
            onShieldDamageRunnable.run();
            return;
        }

        ArmorPlate armorPlate = modules.getArmor().getCell(contactX, contactY);
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
            Vector4f color = ship.getConfigData().getEffectsColor();
            weaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 4.5f, color.x, color.y, color.z, color.w);
            return;
        }

        Vector2 velocity = ship.getLinearVelocity();
        weaponEffects.spawnDirectedSpark(contactX, contactY, normalX, normalY, 3.75f, 1.0f,
                1.0f, 1.0f, 1.0f);
        RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), 0.15f, angleToVelocity);
        garbageSpawner.smallGarbage(random.nextInt(4), contactX, contactY,
                velocity.x * 0.25f + angleToVelocity.x, velocity.y * 0.25f + angleToVelocity.y, 2.0f * random.nextFloat());
    }

    private void setDynamicCorrection(RigidBody rigidBody) {
        CorrectionHandler correctionHandler = rigidBody.getCorrectionHandler();
        if (correctionHandler.getClass() == DynamicCorrectionHandler.class) {
            ((DynamicCorrectionHandler) correctionHandler).setCorrectionAmount(0.0f);
        } else {
            rigidBody.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.1f),
                    correctionHandler, correctionHandler));
        }
    }

    private boolean isShieldAlive(Shield shield) {
        if (shield.isDead()) {
            return false;
        }

        return shield.getShieldHp() > 0;
    }
}