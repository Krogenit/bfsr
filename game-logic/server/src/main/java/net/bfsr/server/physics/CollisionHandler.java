package net.bfsr.server.physics;

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
import net.bfsr.network.packet.server.component.PacketShieldRemove;
import net.bfsr.network.packet.server.entity.PacketSyncDamage;
import net.bfsr.physics.CommonCollisionHandler;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.world.World;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Math;
import org.joml.Vector2f;
import org.locationtech.jts.geom.Polygon;

import java.util.Random;

public class CollisionHandler extends CommonCollisionHandler {
    private final DamageSystem damageSystem = ServerGameLogic.getInstance().getDamageSystem();
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();
    private final Vector2f angleToVelocity = new Vector2f();

    public CollisionHandler(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void bulletRigidBody(Bullet bullet, RigidBody rigidBody, Fixture bulletFixture, Fixture rigidBodyFixture,
                                float contactX, float contactY, float normalX, float normalY) {
        super.bulletRigidBody(bullet, rigidBody, bulletFixture, rigidBodyFixture, contactX, contactY, normalX, normalY);
        damageRigidBody(rigidBody, bullet.getDamage().getHull());
    }

    @Override
    public void bulletShip(Bullet bullet, Ship ship, Fixture bulletFixture, Fixture shipFixture, float contactX,
                           float contactY, float normalX, float normalY) {
        if (bullet.getLastCollidedRigidBody() == ship) return;

        super.bulletShip(bullet, ship, bulletFixture, shipFixture, contactX, contactY, normalX, normalY);

        damageShip(ship, bullet.getDamage(), 1.0f, contactX, contactY, shipFixture, () -> {
            bullet.damage();
            bullet.reflect(normalX, normalY);
        }, bullet::setDead, () -> {
            World world = ship.getWorld();
            Random rand = world.getRand();
            if (rand.nextInt(2) == 0) {
                RotationHelper.angleToVelocity(net.bfsr.engine.math.MathUtils.TWO_PI * rand.nextFloat(), 1.5f, angleToVelocity);
                float velocityX = ship.getLinearVelocity().x * 0.005f;
                float velocityY = ship.getLinearVelocity().y * 0.005f;
                world.getGameLogic().addFutureTask(() -> WreckSpawner.spawnDamageDebris(world, rand.nextInt(2), contactX, contactY,
                        velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.75f));
            }
            bullet.setDead();
        });
    }

    @Override
    public void bulletWreck(Bullet bullet, Wreck wreck, Fixture bulletFixture, Fixture wreckFixture, float contactX,
                            float contactY, float normalX, float normalY) {
        super.bulletWreck(bullet, wreck, bulletFixture, wreckFixture, contactX, contactY, normalX, normalY);
        damageWreck(wreck, bullet.getDamage().getHull());
    }

    @Override
    public void bulletShipWreck(Bullet bullet, ShipWreck wreck, Fixture bulletFixture, Fixture shipWreckFixture,
                                float contactX, float contactY, float normalX, float normalY) {
        super.bulletShipWreck(bullet, wreck, bulletFixture, shipWreckFixture, contactX, contactY, normalX, normalY);
        createDamage(wreck, contactX, contactY);
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
                damageShipByCollision(ship1, ship1Fixture, impactPower, contactX, contactY);
            }

            if (ship2.getCollisionTimer() <= 0) {
                ship2.setCollisionTimer(ship2.getWorld().convertToTicks(0.5f));
                ship2.setLastAttacker(ship1);
                damageShipByCollision(ship2, ship2Fixture, impactPower, contactX, contactY);
            }
        }
    }

    @Override
    public void weaponSlotBeamShip(WeaponSlotBeam weaponSlot, Ship ship, Fixture fixture, float contactX, float contactY,
                                   float normalX, float normalY) {
        damageShip(ship, weaponSlot.getDamage(), weaponSlot.getBeamPower() * ship.getWorld().getUpdateDeltaTime(), contactX,
                contactY, fixture, RunnableUtils.EMPTY_RUNNABLE, RunnableUtils.EMPTY_RUNNABLE, RunnableUtils.EMPTY_RUNNABLE);
    }

    @Override
    public void weaponSlotBeamWreck(WeaponSlotBeam weaponSlotBeam, Wreck wreck, Fixture wreckFixture, float contactX,
                                    float contactY, float normalX, float normalY) {
        super.weaponSlotBeamWreck(weaponSlotBeam, wreck, wreckFixture, contactX, contactY, normalX, normalY);
        damageWreck(wreck, weaponSlotBeam.getDamage().getHull() * weaponSlotBeam.getBeamPower() *
                wreck.getWorld().getUpdateDeltaTime());
    }

    private void damageShip(Ship ship, BulletDamage damage, float multiplayer, float contactX, float contactY,
                            Fixture fixture, Runnable onShieldDamageRunnable, Runnable onArmorDamageRunnable,
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

    private void damageShipByCollision(Ship ship, Fixture fixture, float impactPower, float contactX, float contactY) {
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

        HullCell cell = modules.getHull().damage(impactPower, contactX, contactY, ship);

        Object userData = fixture.getUserData();
        if (userData instanceof DamageableModule) {
            ((DamageableModule) userData).damage(impactPower);
        }

        if (cell.getValue() <= 0 && impactPower > 0.4f) {
            createDamage(ship, contactX, contactY);
        }
    }

    private boolean damageShield(Shield shield, float amount) {
        if (shield.isDead()) return false;

        if (shield.getShieldHp() > 0) {
            shield.setShieldHp(shield.getShieldHp() - amount);

            if (shield.getShieldHp() <= 0) {
                shield.removeShield();
                Ship ship = shield.getShip();
                trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRemove(ship.getId(),
                        ship.getWorld().getTimestamp()));

                shield.setShieldHp(0);
            }

            return true;
        }

        shield.resetRebuildingTime();
        Ship ship = shield.getShip();
        trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRebuildingTime(
                ship.getId(), 0, ship.getWorld().getTimestamp()));
        return false;
    }

    private void damageWreck(Wreck wreck, float amount) {
        damageRigidBody(wreck, amount);
    }

    private void damageRigidBody(RigidBody rigidBody, float amount) {
        float health = rigidBody.getHealth();
        rigidBody.setHealth(health - amount);
        if (health <= 0) {
            rigidBody.setDead();
        }
    }

    private void createDamage(DamageableRigidBody rigidBody, float contactX, float contactY) {
        float x = rigidBody.getX();
        float y = rigidBody.getY();
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();
        float polygonRadius = 0.5f;
        float radius = 1.0f;

        Polygon clip = damageSystem.createCirclePath(contactX - x, contactY - y, -sin, cos, 12, polygonRadius);
        rigidBody.getWorld().getGameLogic().addFutureTask(() -> damageSystem.damage(rigidBody, contactX, contactY, clip, radius, x, y, sin,
                cos, () -> trackingManager.sendPacketToPlayersTrackingEntity(rigidBody.getId(), new PacketSyncDamage(rigidBody,
                        rigidBody.getWorld().getTimestamp()))));
    }
}