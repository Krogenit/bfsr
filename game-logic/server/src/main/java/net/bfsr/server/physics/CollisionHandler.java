package net.bfsr.server.physics;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleCell;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.server.entity.PacketSyncDamage;
import net.bfsr.physics.CommonCollisionHandler;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.world.World;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Math;
import org.joml.Vector2f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

public class CollisionHandler extends CommonCollisionHandler {
    private final DamageSystem damageSystem;
    private final EntityTrackingManager trackingManager;
    private final WreckSpawner wreckSpawner;
    private final Vector2f angleToVelocity = new Vector2f();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    public CollisionHandler(EventBus eventBus, DamageSystem damageSystem, EntityTrackingManager trackingManager,
                            WreckSpawner wreckSpawner) {
        super(eventBus);
        this.damageSystem = damageSystem;
        this.trackingManager = trackingManager;
        this.wreckSpawner = wreckSpawner;
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
        if (bullet.getLastCollidedRigidBody() == ship) {
            return;
        }

        super.bulletShip(bullet, ship, bulletFixture, shipFixture, contactX, contactY, normalX, normalY);

        float clipPolygonRadius = 0.75f;
        float maskClipRadius = 1.0f;
        Polygon clipPolygon = createBulletClipPolygon(contactX - ship.getX(), contactY - ship.getY(), ship.getSin(), ship.getCos(),
                clipPolygonRadius);
        damageShip(ship, bullet.getDamage(), 1.0f, contactX, contactY, shipFixture, () -> {
            bullet.damage();
            bullet.reflect(normalX, normalY);
        }, bullet::setDead, () -> {
            World world = ship.getWorld();
            if (random.nextInt(2) == 0) {
                RotationHelper.angleToVelocity(net.bfsr.engine.math.MathUtils.TWO_PI * random.nextFloat(), 1.5f, angleToVelocity);
                float velocityX = ship.getLinearVelocity().x * 0.005f;
                float velocityY = ship.getLinearVelocity().y * 0.005f;
                wreckSpawner.spawnDamageDebris(world, random.nextInt(2), contactX, contactY,
                        velocityX + angleToVelocity.x, velocityY + angleToVelocity.y, 0.75f);
            }
            bullet.setDead();
        }, clipPolygon, maskClipRadius);
    }

    private Polygon createBulletClipPolygon(float x, float y, float sin, float cos, float radius) {
        return damageSystem.createCirclePath(x, y, -sin, cos, 6, radius);
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
        float clipPolygonRadius = 0.75f;
        float maskClipRadius = 1.0f;
        Polygon clipPolygon = createBulletClipPolygon(contactX - wreck.getX(), contactY - wreck.getY(), wreck.getSin(), wreck.getCos(),
                clipPolygonRadius);
        createDamage(wreck, contactX, contactY, clipPolygon, maskClipRadius);
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
                damageShipByCollision(ship1, ship1Fixture, impactPower, contactX, contactY);
            }

            if (ship2.getCollisionTimer() <= 0) {
                ship2.setCollisionTimer(Engine.convertToTicks(0.5f));
                ship2.setLastAttacker(ship1);
                damageShipByCollision(ship2, ship2Fixture, impactPower, contactX, contactY);
            }
        }
    }

    @Override
    public void weaponSlotBeamShip(WeaponSlotBeam weaponSlot, Ship ship, Fixture fixture, float contactX, float contactY,
                                   float normalX, float normalY) {
        float maskClipRadius = 1.25f;
        float clipRectangleWidth = 1.5f;
        float clipRectangleHeight = 1.0f;
        float penetration = 0.7f;
        float sin = ship.getSin();
        float cos = ship.getCos();
        float localRotatedContactX = contactX - ship.getX();
        float localRotatedContactY = contactY - ship.getY();
        float localContactX = cos * localRotatedContactX + sin * localRotatedContactY;
        float localContactY = cos * localRotatedContactY - sin * localRotatedContactX;
        float localSin = cos * weaponSlot.getSin() - sin * weaponSlot.getCos();
        float localCos = cos * weaponSlot.getCos() + sin * weaponSlot.getSin();

        Polygon clipPolygon = createBeamClipPolygon(clipRectangleWidth, clipRectangleHeight, localContactX, localContactY,
                localSin, localCos, penetration);
        damageShip(ship, weaponSlot.getDamage(), weaponSlot.getBeamPower() * Engine.getUpdateDeltaTime(), contactX, contactY,
                fixture, RunnableUtils.EMPTY_RUNNABLE, RunnableUtils.EMPTY_RUNNABLE, RunnableUtils.EMPTY_RUNNABLE, clipPolygon,
                maskClipRadius);
    }

    @Override
    public void weaponSlotBeamShipWreck(WeaponSlotBeam weaponSlot, ShipWreck wreck, Fixture fixture, float contactX, float contactY,
                                        float normalX, float normalY) {
        float maskClipRadius = 0.75f;
        float clipRectangleWidth = 1.5f;
        float clipRectangleHeight = 1.0f;
        float penetration = 0.7f;
        float sin = wreck.getSin();
        float cos = wreck.getCos();
        float localRotatedContactX = contactX - wreck.getX();
        float localRotatedContactY = contactY - wreck.getY();
        float localContactX = cos * localRotatedContactX + sin * localRotatedContactY;
        float localContactY = cos * localRotatedContactY - sin * localRotatedContactX;
        float localSin = cos * weaponSlot.getSin() - sin * weaponSlot.getCos();
        float localCos = cos * weaponSlot.getCos() + sin * weaponSlot.getSin();

        Polygon clipPolygon = createBeamClipPolygon(clipRectangleWidth, clipRectangleHeight, localContactX, localContactY,
                localSin, localCos, penetration);
        createDamage(wreck, contactX, contactY, clipPolygon, maskClipRadius);
    }

    private Polygon createBeamClipPolygon(float width, float height, float x, float y, float sin, float cos, float penetration) {
        Polygon polygon = damageSystem.createCenteredRectanglePolygon(width, height, x, y, sin, cos);
        float penetrationX = penetration * cos;
        float penetrationY = penetration * sin;

        Coordinate[] coordinates = polygon.getCoordinates();
        for (int i = 0; i < coordinates.length - 1; i++) {
            Coordinate coordinate = coordinates[i];
            coordinate.x += penetrationX;
            coordinate.y += penetrationY;
        }

        return polygon;
    }

    @Override
    public void weaponSlotBeamWreck(WeaponSlotBeam weaponSlotBeam, Wreck wreck, Fixture wreckFixture, float contactX,
                                    float contactY, float normalX, float normalY) {
        super.weaponSlotBeamWreck(weaponSlotBeam, wreck, wreckFixture, contactX, contactY, normalX, normalY);
        damageWreck(wreck, weaponSlotBeam.getDamage().getHull() * weaponSlotBeam.getBeamPower() * Engine.getUpdateDeltaTime());
    }

    private void damageShip(Ship ship, BulletDamage damage, float multiplayer, float contactX, float contactY,
                            Fixture fixture, Runnable onShieldDamageRunnable, Runnable onArmorDamageRunnable,
                            Runnable onHullDamageRunnable, Polygon clipPolygon, float maskClipRadius) {
        Modules modules = ship.getModules();
        Shield shield = modules.getShield();
        if (shield != null && damageShield(shield, damage.getShield() * multiplayer)) {
            onShieldDamageRunnable.run();
            return;
        }

        float armorDamage = damage.getArmor() * multiplayer;
        float hullDamage = damage.getHull() * multiplayer;

        ArmorPlate armorPlate = modules.getArmor().getCell(contactX, contactY);
        if (armorPlate != null && armorPlate.getValue() > 0) {
            armorPlate.damage(armorDamage);
            hullDamage /= armorPlate.getHullProtection();
            onArmorDamageRunnable.run();
        } else {
            onHullDamageRunnable.run();
        }

        Hull hull = modules.getHull();
        HullCell cell = hull.getCell(contactX, contactY);

        if (cell.getValue() > 0.0f) {
            cell.damage(hullDamage);

            if (cell.getValue() <= 0.0f) {
                createCellDamage(hull.getCells(), cell, ship, contactX, contactY);
            }
        } else {
            createDamage(ship, contactX, contactY, clipPolygon, maskClipRadius);
        }

        Object userData = fixture.getUserData();
        if (userData instanceof DamageableModule) {
            ((DamageableModule) userData).damage(hullDamage);
        }
    }

    private void createCellDamage(ModuleCell[][] cells, ModuleCell cell, DamageableRigidBody rigidBody, float contactX, float contactY) {
        int lengthX = cells.length;
        int lengthY = cells[0].length;
        float sizeX = rigidBody.getSizeX();
        float sizeY = rigidBody.getSizeY();
        float halfSizeX = sizeX * 0.5f;
        float halfSizeY = sizeY * 0.5f;
        float rhombusScaleX = RandomHelper.randomFloat(random, 1.1f, 1.6f);
        float rhombusScaleY = RandomHelper.randomFloat(random, 1.1f, 1.6f);
        float rhombusWidth = sizeX / lengthX;
        float rhombusHeight = sizeY / lengthY;
        float halfRhombusWidth = rhombusWidth * 0.5f;
        float halfRhombusHeight = rhombusHeight * 0.5f;
        float posX = cell.getColumn() * rhombusWidth - halfSizeX + halfRhombusWidth;
        float posY = cell.getRow() * rhombusHeight - halfSizeY + halfRhombusHeight;

        Polygon clipPolygon = damageSystem.createCenteredRhombusPolygon(rhombusWidth * rhombusScaleX, rhombusHeight * rhombusScaleY,
                posX, posY, 0, 1);
        damageSystem.damage(rigidBody, contactX, contactY, clipPolygon, java.lang.Math.max(rhombusWidth, rhombusHeight) / 2,
                rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(), rigidBody.getCos(),
                () -> trackingManager.sendPacketToPlayersTrackingEntity(rigidBody.getId(),
                        new PacketSyncDamage(rigidBody, rigidBody.getWorld().getTimestamp())));
    }

    private void damageShipByCollision(Ship ship, Fixture fixture, float impactPower, float contactX, float contactY) {
        Modules modules = ship.getModules();
        Shield shield = modules.getShield();
        if (shield != null && damageShield(shield, impactPower)) {
            return;
        }

        ArmorPlate armorPlate = modules.getArmor().getCell(contactX, contactY);
        if (armorPlate != null && armorPlate.getValue() > 0) {
            armorPlate.damage(impactPower);
            impactPower /= armorPlate.getHullProtection();
        }

        Hull hull = modules.getHull();
        HullCell cell = hull.getCell(contactX, contactY);

        if (impactPower > 0.4f) {
            if (cell.getValue() > 0.0f) {
                cell.damage(impactPower);

                if (cell.getValue() <= 0.0f) {
                    createCellDamage(hull.getCells(), cell, ship, contactX, contactY);
                }
            } else {
                float clipPolygonRadius = 0.5f;
                float maskClipRadius = 0.75f;
                Polygon clipPolygon = createBulletClipPolygon(contactX - ship.getX(), contactY - ship.getY(), ship.getSin(), ship.getCos(),
                        clipPolygonRadius);
                createDamage(ship, contactX, contactY, clipPolygon, maskClipRadius);
            }

            Object userData = fixture.getUserData();
            if (userData instanceof DamageableModule) {
                ((DamageableModule) userData).damage(impactPower);
            }
        }
    }

    private boolean damageShield(Shield shield, float amount) {
        if (shield.isDead()) return false;

        if (shield.getShieldHp() > 0) {
            shield.damageShield(amount);
            return true;
        }

        shield.resetRebuildingTime();
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

    private void createDamage(DamageableRigidBody rigidBody, float contactX, float contactY, Polygon clipPolygon, float maskClipRadius) {
        float x = rigidBody.getX();
        float y = rigidBody.getY();
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();

        damageSystem.damage(rigidBody, contactX, contactY, clipPolygon, maskClipRadius, x, y, sin, cos,
                () -> trackingManager.sendPacketToPlayersTrackingEntity(rigidBody.getId(),
                        new PacketSyncDamage(rigidBody, rigidBody.getWorld().getTimestamp())));
    }
}