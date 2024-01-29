package net.bfsr.server.physics;

import clipper2.core.Path64;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.server.component.PacketShieldRebuildingTime;
import net.bfsr.physics.CommonCollisionHandler;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Transform;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.Random;

public class CollisionHandler extends CommonCollisionHandler {
    private final DamageSystem damageSystem = ServerGameLogic.getInstance().getDamageSystem();
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();
    private final Vector2f angleToVelocity = new Vector2f();

    public CollisionHandler(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void bulletRigidBody(Bullet bullet, RigidBody<?> rigidBody, BodyFixture bulletFixture, BodyFixture rigidBodyFixture,
                                float contactX, float contactY, float normalX, float normalY,
                                ContactCollisionData<Body> collision) {
        super.bulletRigidBody(bullet, rigidBody, bulletFixture, rigidBodyFixture, contactX, contactY, normalX, normalY,
                collision);
        damageRigidBody(rigidBody, bullet.getDamage().getHull());
    }

    @Override
    public void bulletShip(Bullet bullet, Ship ship, BodyFixture bulletFixture, BodyFixture shipFixture, float contactX,
                           float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        if (bullet.getLastCollidedRigidBody() == ship) return;

        super.bulletShip(bullet, ship, bulletFixture, shipFixture, contactX, contactY, normalX, normalY, collision);

        damageShip(ship, bullet.getDamage(), 1.0f, contactX, contactY, shipFixture, () -> {
            bullet.damage();
            bullet.reflect(normalX, normalY);
        }, bullet::setDead, () -> {
            World world = ship.getWorld();
            Random rand = world.getRand();
            if (rand.nextInt(2) == 0) {
                RotationHelper.angleToVelocity(net.bfsr.engine.math.MathUtils.TWO_PI * rand.nextFloat(), 1.5f, angleToVelocity);
                float velocityX = ship.getVelocity().x * 0.005f;
                float velocityY = ship.getVelocity().y * 0.005f;
                WreckSpawner.spawnDamageDebris(world, rand.nextInt(2), contactX, contactY,
                        velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.75f);
            }
            bullet.setDead();
        });
    }

    @Override
    public void bulletWreck(Bullet bullet, Wreck wreck, BodyFixture bulletFixture, BodyFixture wreckFixture, float contactX,
                            float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        super.bulletWreck(bullet, wreck, bulletFixture, wreckFixture, contactX, contactY, normalX, normalY, collision);
        damageWreck(wreck, bullet.getDamage().getHull());
    }

    @Override
    public void bulletShipWreck(Bullet bullet, ShipWreck wreck, BodyFixture bulletFixture, BodyFixture shipWreckFixture,
                                float contactX, float contactY, float normalX, float normalY,
                                ContactCollisionData<Body> collision) {
        super.bulletShipWreck(bullet, wreck, bulletFixture, shipWreckFixture, contactX, contactY, normalX, normalY, collision);
        createDamage(wreck, contactX, contactY);
    }

    @Override
    public void shipShip(Ship ship1, Ship ship2, BodyFixture ship1Fixture, BodyFixture ship2Fixture, float contactX,
                         float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        float dx = ship2.getVelocity().x - ship1.getVelocity().x;
        float dy = ship2.getVelocity().y - ship1.getVelocity().y;
        float impactPowerForOther = (float) ((Math.sqrt(dx * dx + dy * dy)) *
                (ship1.getBody().getMass().getMass() / ship2.getBody().getMass().getMass()));

        impactPowerForOther /= 400.0f;

        if (impactPowerForOther > 0.25f) {
            ship1.setLastAttacker(ship2);
            ship2.setLastAttacker(ship1);
            damageShipByCollision(ship1, ship1Fixture, impactPowerForOther, contactX, contactY);
            damageShipByCollision(ship2, ship2Fixture, impactPowerForOther, contactX, contactY);
        }
    }

    @Override
    public void weaponSlotBeamShip(WeaponSlotBeam weaponSlot, Ship ship, BodyFixture fixture, float contactX, float contactY,
                                   float normalX, float normalY) {
        damageShip(ship, weaponSlot.getDamage(), weaponSlot.getBeamPower() * ship.getWorld().getUpdateDeltaTime(), contactX,
                contactY, fixture, RunnableUtils.EMPTY_RUNNABLE, RunnableUtils.EMPTY_RUNNABLE, RunnableUtils.EMPTY_RUNNABLE);
    }

    @Override
    public void weaponSlotBeamWreck(WeaponSlotBeam weaponSlotBeam, Wreck wreck, BodyFixture wreckFixture, float contactX,
                                    float contactY, float normalX, float normalY) {
        super.weaponSlotBeamWreck(weaponSlotBeam, wreck, wreckFixture, contactX, contactY, normalX, normalY);
        damageWreck(wreck, weaponSlotBeam.getDamage().getHull() * weaponSlotBeam.getBeamPower() *
                wreck.getWorld().getUpdateDeltaTime());
    }

    private void damageShip(Ship ship, BulletDamage damage, float multiplayer, float contactX, float contactY,
                            BodyFixture fixture, Runnable onShieldDamageRunnable, Runnable onArmorDamageRunnable,
                            Runnable onHullDamageRunnable) {
        Modules modules = ship.getModules();
        Shield shield = modules.getShield();
        if (shield != null && damageShield(shield, damage.getShield() * multiplayer)) {
            onShieldDamageRunnable.run();
            return;
        }

        float armorDamage = damage.getArmor() * multiplayer;
        float hullDamage = damage.getHull() * multiplayer;

        ArmorPlate armorPlate = modules.getArmor().getCell(contactX, contactY, ship);
        if (armorPlate != null && armorPlate.getValue() > 0) {
            armorPlate.damage(armorDamage);
            hullDamage /= armorPlate.getHullProtection();
            onArmorDamageRunnable.run();
        } else {
            onHullDamageRunnable.run();
        }

        HullCell cell = modules.getHull().damage(hullDamage, contactX, contactY, ship);

        Object userData = fixture.getUserData();
        if (userData instanceof DamageableModule) {
            ((DamageableModule) userData).damage(hullDamage);
        }

        if (cell.getValue() <= 0) {
            createDamage(ship, contactX, contactY);
        }
    }

    private void damageShipByCollision(Ship ship, BodyFixture fixture, float impactPower, float contactX, float contactY) {
        Modules modules = ship.getModules();
        Shield shield = modules.getShield();
        if (shield != null && damageShield(shield, impactPower)) {
            return;
        }

        ArmorPlate armorPlate = modules.getArmor().getCell(contactX, contactY, ship);
        if (armorPlate != null && armorPlate.getValue() > 0) {
            armorPlate.damage(impactPower);
            impactPower /= armorPlate.getHullProtection();
        }

        modules.getHull().damage(impactPower, contactX, contactY, ship);

        Object userData = fixture.getUserData();
        if (userData instanceof DamageableModule) {
            ((DamageableModule) userData).damage(impactPower);
        }
    }

    private boolean damageShield(Shield shield, float amount) {
        if (shield.isDead()) return false;

        if (shield.getShieldHp() > 0) {
            shield.setShieldHp(shield.getShieldHp() - amount);

            if (shield.getShieldHp() < 0) {
                shield.setShieldHp(0);
            }

            return true;
        }

        shield.resetRebuildingTime();
        Ship ship = shield.getShip();
        trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRebuildingTime(ship.getId(), 0,
                ship.getWorld().getTimestamp()));
        return false;
    }

    public void damageWreck(Wreck wreck, float amount) {
        damageRigidBody(wreck, amount);
    }

    private void damageRigidBody(RigidBody<?> rigidBody, float amount) {
        float health = rigidBody.getHealth();
        rigidBody.setHealth(health - amount);
        if (health <= 0) {
            rigidBody.setDead();
        }
    }

    private void createDamage(DamageableRigidBody<?> rigidBody, float contactX, float contactY) {
        Transform transform = rigidBody.getBody().getTransform();
        double x = transform.getTranslationX();
        double y = transform.getTranslationY();
        double sin = transform.getSint();
        double cos = transform.getCost();
        float polygonRadius = 0.5f;
        float radius = 1.0f;

        Path64 clip = damageSystem.createCirclePath(contactX - x, contactY - y, -sin, cos, 12, polygonRadius);
        damageSystem.damage(rigidBody, contactX, contactY, clip, radius);
    }
}