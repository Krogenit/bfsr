package net.bfsr.server.ai.task;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.RequiredArgsConstructor;
import net.bfsr.ai.task.AiTask;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.module.weapon.WeaponType;
import net.bfsr.network.packet.server.component.PacketWeaponSlotShoot;
import net.bfsr.server.entity.EntityTrackingManager;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AiAttackTarget extends AiTask {
    private final float maxAttackRange;
    private int changeDirTimer;
    private final List<Direction> directionsToAdd = new ArrayList<>(5);
    private Direction sideDirection;
    private final AABB targetAABB = new AABB();
    private final Vector2f targetPos = new Vector2f();
    private final Vector2f targetFinalPos = new Vector2f();
    private final Vector2f totalTargetVelocity = new Vector2f();
    private final Vector2f bulletFinalPos = new Vector2f();
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final EntityTrackingManager trackingManager;
    private final Vector2f rotatedVector = new Vector2f();
    private final Vector2f pointToRotate = new Vector2f();
    private final AABB cache = new AABB();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final Vector2 rayStart = new Vector2();
    private final Vector2 rayDirection = new Vector2();

    @Override
    public void execute() {
        RigidBody target = ship.getTarget();
        float targetSizeX = target.getSizeX();
        float targetSizeY = target.getSizeY();
        float targetSizeAverage;

        if (target.getSizeX() >= 20.0f || target.getSizeY() >= 20.0f) {
            World world = target.getWorld();

            rayStart.x = ship.getX();
            rayStart.y = ship.getY();
            rayDirection.set(target.getX(), target.getY());

            world.getPhysicWorld().raycast((fixture, point, normal, fraction) -> {
                if (fixture.getBody().getUserData() == target) {
                    targetPos.set(point.x, point.y);
                    return fraction;
                } else {
                    return -1.0f;
                }
            }, rayStart, rayDirection);

            targetSizeAverage = 0.0f;
        } else {
            MathUtils.computeAABB(targetAABB, target.getBody(), target.getX(), target.getY(), target.getSin(), target.getCos(), cache);
            targetPos.set((targetAABB.getMinX() + targetAABB.getMaxX()) / 2,
                    (targetAABB.getMinY() + targetAABB.getMaxY()) / 2);
            targetSizeAverage = (targetSizeX + targetSizeY) / 2.0f;
        }

        float x = ship.getX();
        float y = ship.getY();
        float distanceToTarget = targetPos.distance(x, y);
        if (distanceToTarget >= maxAttackRange || target.isDead() || Math.abs(targetPos.x) > 1000 ||
                Math.abs(targetPos.y) > 1000) {
            ship.setTarget(null);
            return;
        }

        float sizeX = ship.getSizeX();
        float sizeY = ship.getSizeY();
        float shipSizeAverage = (sizeX + sizeY) / 2.0f;
        float minTargetToShip = Float.MAX_VALUE;

        float maxDistance = 0;

        Vector2 targetVelocity = target.getLinearVelocity();

        directionsToAdd.clear();

        Modules modules = ship.getModules();
        List<WeaponSlot> slots = modules.getWeaponSlots();
        Engines engines = modules.getEngines();
        if (slots.size() > 0) {
            float minReloadTimer = Float.MAX_VALUE;
            for (int i = 0, size = slots.size(); i < size; i++) {
                WeaponSlot slot = slots.get(i);
                float gunEffectiveDistance;
                Vector2f slotPos = slot.getLocalPosition();
                float cos = ship.getCos();
                float sin = ship.getSin();
                float xPos = cos * slotPos.x - sin * slotPos.y;
                float yPos = sin * slotPos.x + cos * slotPos.y;

                if (slot.getWeaponType() == WeaponType.BEAM) {
                    gunEffectiveDistance = ((WeaponSlotBeam) slot).getBeamMaxRange();
                    targetFinalPos.set(targetPos.x - xPos, targetPos.y - yPos);
                } else {
                    GunData gunData = slot.getGunData();
                    float bulletSpeed = gunData.getBulletSpeed();
                    int totalIterations = gunData.getBulletLifeTimeInTicks();

                    float totalVelocityX = -cos * bulletSpeed * Engine.getUpdateDeltaTime() * totalIterations;
                    float totalVelocityY = -sin * bulletSpeed * Engine.getUpdateDeltaTime() * totalIterations;
                    bulletFinalPos.set(x + xPos + totalVelocityX, y + yPos + totalVelocityY);

                    gunEffectiveDistance = bulletFinalPos.distance(x, y) - 2.0f;

                    if (distanceToTarget < gunEffectiveDistance) {
                        float iterations = distanceToTarget / gunEffectiveDistance;
                        totalIterations = Math.round(totalIterations * iterations);
                    }

                    totalTargetVelocity.set(targetVelocity.x * Engine.getUpdateDeltaTime(),
                            targetVelocity.y * Engine.getUpdateDeltaTime()).mul(totalIterations);
                    targetFinalPos.set(targetPos.x + totalTargetVelocity.x - xPos,
                            targetPos.y + totalTargetVelocity.y - yPos);
                }

                float finalDistanceToTarget = targetFinalPos.distance(x, y);

                if (finalDistanceToTarget <= gunEffectiveDistance) {
                    if (Math.abs(rigidBodyUtils.getRotationDifference(ship, targetFinalPos)) <= 0.05f) {
                        slot.tryShoot(weaponSlot -> {
                            weaponSlot.createBullet(0);
                            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketWeaponSlotShoot(
                                    ship.getId(), weaponSlot.getId(), ship.getWorld().getTimestamp()));
                        }, modules.getReactor());
                    }
                }

                if (slot.getReloadTimer() < minReloadTimer) {
                    pointToRotate.set(targetFinalPos);
                    minReloadTimer = slot.getReloadTimer();
                }

                if (gunEffectiveDistance > maxDistance)
                    maxDistance = gunEffectiveDistance;

                if (finalDistanceToTarget < minTargetToShip)
                    minTargetToShip = finalDistanceToTarget;
            }

            rotatedVector.set(pointToRotate);
            if (minTargetToShip >= maxDistance - targetSizeAverage - shipSizeAverage) {
                if (!engines.isEngineAlive(Direction.FORWARD)) {
                    if (engines.isEngineAlive(Direction.RIGHT)) {
                        RotationHelper.rotate(-1.0f, 0.0f, rotatedVector.x - x,
                                rotatedVector.y - y, rotatedVector);
                        rotatedVector.add(x, y);
                    } else if (engines.isEngineAlive(Direction.LEFT)) {
                        RotationHelper.rotate(1.0f, 0.0f, rotatedVector.x - x, rotatedVector.y - y,
                                rotatedVector);
                        rotatedVector.add(x, y);
                    } else if (engines.isEngineAlive(Direction.BACKWARD)) {
                        RotationHelper.rotate(0.0f, -1.0f, rotatedVector.x - x, rotatedVector.y - y,
                                rotatedVector);
                        rotatedVector.add(x, y);
                    }
                }

                rigidBodyUtils.rotateToVector(ship, rotatedVector, engines.getAngularVelocity());
                List<Direction> dirs = rigidBodyUtils.calculateDirectionsToPoint(ship, pointToRotate);

                for (int i = 0; i < dirs.size(); i++) {
                    Direction direction = dirs.get(i);

                    if (engines.isEngineAlive(direction)) {
                        directionsToAdd.add(direction);
                    }
                }

                sideDirection = null;
            } else if (distanceToTarget < maxDistance - targetSizeAverage - shipSizeAverage) {
                rigidBodyUtils.rotateToVector(ship, pointToRotate, engines.getAngularVelocity());
                Direction dir = Direction.inverse(rigidBodyUtils.calculateDirectionToPoint(ship, pointToRotate));

                if (engines.isEngineAlive(dir)) {
                    directionsToAdd.add(dir);
                }

                boolean isLeftEnginesAlive = engines.isEngineAlive(Direction.LEFT);
                boolean isRightEnginesAlive = engines.isEngineAlive(Direction.RIGHT);
                if (isLeftEnginesAlive && isRightEnginesAlive) {
                    if (changeDirTimer > 0 && sideDirection != null) {
                        changeDirTimer -= 1;
                    } else {
                        if (random.nextInt(2) == 0) {
                            sideDirection = Direction.LEFT;
                        } else {
                            sideDirection = Direction.RIGHT;
                        }

                        changeDirTimer = Engine.convertToTicks(1 + random.nextFloat() * 2);
                    }
                } else {
                    if (isLeftEnginesAlive) {
                        sideDirection = Direction.RIGHT;
                    } else if (isRightEnginesAlive) {
                        sideDirection = Direction.LEFT;
                    }
                }
            } else {
                boolean isLeftEnginesAlive = engines.isEngineAlive(Direction.LEFT);
                boolean isRightEnginesAlive = engines.isEngineAlive(Direction.RIGHT);

                if (isLeftEnginesAlive && isRightEnginesAlive) {
                    if (changeDirTimer > 0 && sideDirection != null) {
                        changeDirTimer -= 1;
                    } else {
                        if (random.nextInt(2) == 0) {
                            sideDirection = Direction.LEFT;
                        } else {
                            sideDirection = Direction.RIGHT;
                        }

                        changeDirTimer = Engine.convertToTicks(1 + random.nextFloat() * 2);
                    }
                } else {
                    if (isLeftEnginesAlive) {
                        sideDirection = Direction.RIGHT;
                    } else if (isRightEnginesAlive) {
                        sideDirection = Direction.LEFT;
                    }
                }

                rigidBodyUtils.rotateToVector(ship, pointToRotate, engines.getAngularVelocity());
            }

            if (sideDirection != null) {
                directionsToAdd.add(sideDirection);
            }
        } else {
            rotatedVector.set(targetPos);

            if (!engines.isEngineAlive(Direction.FORWARD)) {
                if (engines.isEngineAlive(Direction.RIGHT)) {
                    RotationHelper.rotate(-1.0f, 0.0f, rotatedVector.x - x, rotatedVector.y - y,
                            rotatedVector);
                    rotatedVector.add(x, y);
                } else if (engines.isEngineAlive(Direction.LEFT)) {
                    RotationHelper.rotate(1.0f, 0.0f, rotatedVector.x - x, rotatedVector.y - y,
                            rotatedVector);
                    rotatedVector.add(x, y);
                } else if (engines.isEngineAlive(Direction.BACKWARD)) {
                    RotationHelper.rotate(0.0f, -1.0f, rotatedVector.x - x, rotatedVector.y - y,
                            rotatedVector);
                    rotatedVector.add(x, y);
                }
            }

            rigidBodyUtils.rotateToVector(ship, rotatedVector, engines.getAngularVelocity());

            List<Direction> dirs = rigidBodyUtils.calculateDirectionsToPoint(ship, targetPos);
            for (int i = 0; i < dirs.size(); i++) {
                Direction direction = dirs.get(i);

                if (engines.isEngineAlive(direction)) {
                    directionsToAdd.add(direction);
                }
            }

            sideDirection = null;
        }

        for (int i = 0; i < Direction.VALUES.length; i++) {
            Direction direction = Direction.VALUES[i];
            if (!directionsToAdd.contains(direction)) {
                ship.removeMoveDirection(direction);
            } else {
                ship.addMoveDirection(direction);
            }
        }

        ship.getMoveDirections().forEach(ship::move);
    }

    @Override
    public boolean shouldExecute() {
        return ship.getTarget() != null && !ship.getTarget().isDead();
    }
}