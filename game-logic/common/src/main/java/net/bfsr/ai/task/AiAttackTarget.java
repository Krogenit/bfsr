package net.bfsr.ai.task;

import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.Engine;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.module.weapon.WeaponType;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AiAttackTarget extends AiTask {
    private final float maxAttackRange;
    private int changeDirTimer;
    private final List<Direction> directionsToAdd = new ArrayList<>(5);
    private Direction sideDirection;
    private final AABB targetAABB = new AABB(0, 0, 0, 0);
    private final Vector2f targetPos = new Vector2f();
    private final Vector2f targetFinalPos = new Vector2f();
    private final Vector2f totalTargetVelocity = new Vector2f();
    private final Vector2f bulletFinalPos = new Vector2f();

    public AiAttackTarget(Ship ship, float maxAttackRange) {
        super(ship);
        this.maxAttackRange = maxAttackRange;
    }

    @Override
    public void execute() {
        RigidBody<?> target = ship.getTarget();
        target.getBody().computeAABB(targetAABB);
        targetPos.set((float) ((targetAABB.getMinX() + targetAABB.getMaxX()) / 2),
                (float) ((targetAABB.getMinY() + targetAABB.getMaxY()) / 2));
        Vector2f pos = ship.getPosition();

        float distanceToTarget = targetPos.distance(pos.x, pos.y);
        if (distanceToTarget >= maxAttackRange || target.isDead() || Math.abs(targetPos.x) > 1000 ||
                Math.abs(targetPos.y) > 1000) {
            ship.setTarget(null);
            return;
        }

        Vector2f shipSize = ship.getSize();
        float shipSizeAverage = (shipSize.x + shipSize.y) / 2.0f;
        float minTargetToShip = Float.MAX_VALUE;

        float maxDistance = 0;
        Vector2f pointToRotate = null;
        Vector2f targetSize = target.getSize();
        float targetSizeAverage = (targetSize.x + targetSize.y) / 2.0f;

        Vector2f targetVelocity = target.getVelocity();

        directionsToAdd.clear();

        Modules modules = ship.getModules();
        List<WeaponSlot> slots = modules.getWeaponSlots();
        Engines engines = modules.getEngines();
        if (slots.size() > 0) {
            float minReloadTimer = Float.MAX_VALUE;
            for (int i = 0, size = slots.size(); i < size; i++) {
                WeaponSlot slot = slots.get(i);
                float bulletToShip;
                Vector2f slotPos = slot.getLocalPosition();
                float cos = ship.getCos();
                float sin = ship.getSin();
                float xPos = cos * slotPos.x - sin * slotPos.y;
                float yPos = sin * slotPos.x + cos * slotPos.y;

                if (slot.getWeaponType() == WeaponType.BEAM) {
                    bulletToShip = ((WeaponSlotBeam) slot).getBeamMaxRange();
                    targetFinalPos.set(targetPos.x - xPos, targetPos.y - yPos);
                } else {
                    GunData gunData = slot.getGunData();
                    float bulletSpeed = gunData.getBulletSpeed();
                    int totalIterations = gunData.getBulletLifeTimeInTicks();

                    float totalVelocityX = -cos * bulletSpeed * Engine.getUpdateDeltaTime() * totalIterations;
                    float totalVelocityY = -sin * bulletSpeed * Engine.getUpdateDeltaTime() * totalIterations;
                    bulletFinalPos.set(pos.x - xPos + totalVelocityX, pos.y - yPos + totalVelocityY);

                    bulletToShip = bulletFinalPos.distance(pos) - 2.0f;

                    if (distanceToTarget < bulletToShip) {
                        totalIterations *= distanceToTarget / bulletToShip;
                    }

                    totalTargetVelocity.set(targetVelocity.x * Engine.getUpdateDeltaTime(),
                            targetVelocity.y * Engine.getUpdateDeltaTime()).mul(totalIterations);
                    targetFinalPos.set(targetPos.x + totalTargetVelocity.x - xPos,
                            targetPos.y + totalTargetVelocity.y - yPos);
                }

                float targetToShip = targetFinalPos.distance(pos);

                if (targetToShip <= bulletToShip) {
                    if (Math.abs(RigidBodyUtils.getRotationDifference(ship, targetFinalPos)) <= 0.1f + shipSizeAverage / 25.0f) {
                        slot.tryShoot(weaponSlot -> ship.getWorld().getEventBus().publish(new WeaponShotEvent(weaponSlot)),
                                modules.getReactor());
                    }
                }

                if (slot.getReloadTimer() < minReloadTimer) {
                    pointToRotate = targetFinalPos;
                    minReloadTimer = slot.getReloadTimer();
                }

                if (bulletToShip > maxDistance)
                    maxDistance = bulletToShip;

                if (targetToShip < minTargetToShip)
                    minTargetToShip = targetToShip;
            }

            Vector2f finalPointToRotate = Objects.requireNonNullElse(pointToRotate, targetPos);

            if (!engines.isEngineAlive(Direction.FORWARD)) {
                if (engines.isEngineAlive(Direction.RIGHT)) {
                    finalPointToRotate = RotationHelper.rotate(-1, 0, finalPointToRotate.x - pos.x,
                            finalPointToRotate.y - pos.y);
                    finalPointToRotate.add(pos.x, pos.y);
                } else if (engines.isEngineAlive(Direction.LEFT)) {
                    finalPointToRotate = RotationHelper.rotate(1, 0, finalPointToRotate.x - pos.x,
                            finalPointToRotate.y - pos.y);
                    finalPointToRotate.add(pos.x, pos.y);
                } else if (engines.isEngineAlive(Direction.BACKWARD)) {
                    finalPointToRotate = RotationHelper.rotate(0, -1, finalPointToRotate.x - pos.x,
                            finalPointToRotate.y - pos.y);
                    finalPointToRotate.add(pos.x, pos.y);
                }
            }

            RigidBodyUtils.rotateToVector(ship, finalPointToRotate, engines.getAngularVelocity());

            if (minTargetToShip >= maxDistance - targetSizeAverage - shipSizeAverage) {
                List<Direction> dirs = RigidBodyUtils.calculateDirectionsToOtherObject(ship, targetPos.x, targetPos.y);

                for (int i = 0; i < dirs.size(); i++) {
                    Direction direction = dirs.get(i);

                    if (engines.isEngineAlive(direction)) {
                        directionsToAdd.add(direction);
                    }
                }

                sideDirection = null;
            } else if (distanceToTarget < maxDistance - targetSizeAverage - shipSizeAverage - 100) {
                Direction dir = Direction.inverse(RigidBodyUtils.calculateDirectionToOtherObject(ship, targetPos.x, targetPos.y));

                if (engines.isEngineAlive(dir)) {
                    directionsToAdd.add(dir);
                }

                boolean isLeftEnginesAlive = engines.isEngineAlive(Direction.LEFT);
                boolean isRightEnginesAlive = engines.isEngineAlive(Direction.RIGHT);
                if (isLeftEnginesAlive && isRightEnginesAlive) {
                    if (changeDirTimer > 0 && sideDirection != null) {
                        changeDirTimer -= 1;
                    } else {
                        Random rand = ship.getWorld().getRand();
                        if (rand.nextInt(2) == 0) {
                            sideDirection = Direction.LEFT;
                        } else {
                            sideDirection = Direction.RIGHT;
                        }

                        changeDirTimer = Engine.convertToTicks(1 + rand.nextFloat() * 2);
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
                        Random rand = ship.getWorld().getRand();
                        if (rand.nextInt(2) == 0) {
                            sideDirection = Direction.LEFT;
                        } else {
                            sideDirection = Direction.RIGHT;
                        }

                        changeDirTimer = Engine.convertToTicks(1 + rand.nextFloat() * 2);
                    }
                } else {
                    if (isLeftEnginesAlive) {
                        sideDirection = Direction.RIGHT;
                    } else if (isRightEnginesAlive) {
                        sideDirection = Direction.LEFT;
                    }
                }
            }

            if (sideDirection != null) {
                directionsToAdd.add(sideDirection);
            }
        } else {
            Vector2f finalPointToRotate = Objects.requireNonNullElse(pointToRotate, targetPos);

            if (!engines.isEngineAlive(Direction.FORWARD)) {
                if (engines.isEngineAlive(Direction.RIGHT)) {
                    finalPointToRotate = RotationHelper.rotate(-1, 0, finalPointToRotate.x - pos.x,
                            finalPointToRotate.y - pos.y);
                    finalPointToRotate.add(pos.x, pos.y);
                } else if (engines.isEngineAlive(Direction.LEFT)) {
                    finalPointToRotate = RotationHelper.rotate(1, 0, finalPointToRotate.x - pos.x,
                            finalPointToRotate.y - pos.y);
                    finalPointToRotate.add(pos.x, pos.y);
                } else if (engines.isEngineAlive(Direction.BACKWARD)) {
                    finalPointToRotate = RotationHelper.rotate(0, -1, finalPointToRotate.x - pos.x,
                            finalPointToRotate.y - pos.y);
                    finalPointToRotate.add(pos.x, pos.y);
                }
            }

            RigidBodyUtils.rotateToVector(ship, finalPointToRotate, engines.getAngularVelocity());

            List<Direction> dirs = RigidBodyUtils.calculateDirectionsToOtherObject(ship, targetPos.x, targetPos.y);
            for (int i = 0; i < dirs.size(); i++) {
                Direction direction = dirs.get(i);

                if (engines.isEngineAlive(direction)) {
                    directionsToAdd.add(direction);
                }
            }

            sideDirection = null;
        }

        Direction[] directions = Direction.values();
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
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