package net.bfsr.server.ai.task;

import lombok.RequiredArgsConstructor;
import net.bfsr.ai.task.AiTask;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.Engine;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.module.weapon.WeaponType;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
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
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();
    private final Vector2f rotatedVector = new Vector2f();
    private final Vector2f pointToRotate = new Vector2f();

    @Override
    public void execute() {
        RigidBody target = ship.getTarget();
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
                    bulletFinalPos.set(pos.x + xPos + totalVelocityX, pos.y + yPos + totalVelocityY);

                    gunEffectiveDistance = bulletFinalPos.distance(pos) - 2.0f;

                    if (distanceToTarget < gunEffectiveDistance) {
                        totalIterations *= distanceToTarget / gunEffectiveDistance;
                    }

                    totalTargetVelocity.set(targetVelocity.x * Engine.getUpdateDeltaTime(),
                            targetVelocity.y * Engine.getUpdateDeltaTime()).mul(totalIterations);
                    targetFinalPos.set(targetPos.x + totalTargetVelocity.x - xPos,
                            targetPos.y + totalTargetVelocity.y - yPos);
                }

                float finalDistanceToTarget = targetFinalPos.distance(pos);

                if (finalDistanceToTarget <= gunEffectiveDistance) {
                    if (Math.abs(rigidBodyUtils.getRotationDifference(ship, targetFinalPos)) <= 0.05f) {
                        slot.tryShoot(weaponSlot -> {
                            weaponSlot.createBullet(0);
                            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketWeaponShoot(ship.getId(),
                                    weaponSlot.getId(), ship.getWorld().getTimestamp()));
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
                        RotationHelper.rotate(-1.0f, 0.0f, rotatedVector.x - pos.x,
                                rotatedVector.y - pos.y, rotatedVector);
                        rotatedVector.add(pos.x, pos.y);
                    } else if (engines.isEngineAlive(Direction.LEFT)) {
                        RotationHelper.rotate(1.0f, 0.0f, rotatedVector.x - pos.x, rotatedVector.y - pos.y,
                                rotatedVector);
                        rotatedVector.add(pos.x, pos.y);
                    } else if (engines.isEngineAlive(Direction.BACKWARD)) {
                        RotationHelper.rotate(0.0f, -1.0f, rotatedVector.x - pos.x, rotatedVector.y - pos.y,
                                rotatedVector);
                        rotatedVector.add(pos.x, pos.y);
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

                rigidBodyUtils.rotateToVector(ship, pointToRotate, engines.getAngularVelocity());
            }

            if (sideDirection != null) {
                directionsToAdd.add(sideDirection);
            }
        } else {
            rotatedVector.set(targetPos);

            if (!engines.isEngineAlive(Direction.FORWARD)) {
                if (engines.isEngineAlive(Direction.RIGHT)) {
                    RotationHelper.rotate(-1.0f, 0.0f, rotatedVector.x - pos.x, rotatedVector.y - pos.y,
                            rotatedVector);
                    rotatedVector.add(pos.x, pos.y);
                } else if (engines.isEngineAlive(Direction.LEFT)) {
                    RotationHelper.rotate(1.0f, 0.0f, rotatedVector.x - pos.x, rotatedVector.y - pos.y,
                            rotatedVector);
                    rotatedVector.add(pos.x, pos.y);
                } else if (engines.isEngineAlive(Direction.BACKWARD)) {
                    RotationHelper.rotate(0.0f, -1.0f, rotatedVector.x - pos.x, rotatedVector.y - pos.y,
                            rotatedVector);
                    rotatedVector.add(pos.x, pos.y);
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