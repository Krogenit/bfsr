package net.bfsr.ai.task;

import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.component.weapon.WeaponType;
import net.bfsr.config.entity.bullet.BulletData;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
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

    public AiAttackTarget(Ship ship, float maxAttackRange) {
        super(ship);
        this.maxAttackRange = maxAttackRange;
    }

    @Override
    public void execute() {
        RigidBody target = ship.getTarget();
        Vector2f targetPos = target.getPosition();
        Vector2f pos = ship.getPosition();

        if (targetPos.distance(pos) >= maxAttackRange || target.isDead()) {
            ship.setTarget(null);
            return;
        }

        float distanceToTarget = targetPos.distance(pos);

        Vector2f shipSize = ship.getSize();
        float shipSizeAverage = (shipSize.x + shipSize.y) / 2.0f;
        float minTargetToShip = Float.MAX_VALUE;

        float maxDistance = 0;
        Vector2f pointToRotate = null;
        Vector2f targetSize = target.getSize();
        float targetSizeAverage = (targetSize.x + targetSize.y) / 2.0f;

        Vector2f targetVelocity = target.getVelocity();

        List<WeaponSlot> slots = ship.getWeaponSlots();
        for (int i = 0, size = slots.size(); i < size; i++) {
            WeaponSlot slot = slots.get(i);
            float bulletToShip;
            Vector2f targetFinalPos;

            if (slot.getType() == WeaponType.BEAM) {
                bulletToShip = ((WeaponSlotBeam) slot).getBeamMaxRange();
                targetFinalPos = new Vector2f(targetPos.x + 0, targetPos.y + 0);
            } else {
                Vector2f slotPos = slot.getLocalPosition();
                float cos = ship.getCos();
                float sin = ship.getSin();
                float xPos = cos * slotPos.x - sin * slotPos.y;
                float yPos = sin * slotPos.x + cos * slotPos.y;

                BulletData bulletData = slot.getBulletData();
                float bulletSpeed = bulletData.getBulletSpeed();
                int totalIterations = bulletData.getLifeTimeInTicks();

                Vector2f totalVelocity = new Vector2f(-cos, -sin).mul(bulletSpeed * TimeUtils.UPDATE_DELTA_TIME).mul(totalIterations);
                Vector2f bulletFinalPos = new Vector2f(pos.x + xPos + totalVelocity.x, pos.y + yPos + totalVelocity.y);

                bulletToShip = bulletFinalPos.distance(pos) - 2.0f;

                if (distanceToTarget < bulletToShip) {
                    totalIterations *= distanceToTarget / bulletToShip;
                }

                Vector2f totalTargetVelocity = new Vector2f(targetVelocity.x * TimeUtils.UPDATE_DELTA_TIME, targetVelocity.y * TimeUtils.UPDATE_DELTA_TIME).mul(totalIterations);
                targetFinalPos = new Vector2f(targetPos.x + totalTargetVelocity.x, targetPos.y + totalTargetVelocity.y);
            }

            float targetToShip = targetFinalPos.distance(pos);

            if (targetToShip <= bulletToShip) {
                if (Math.abs(MathUtils.getRotationDifference(ship, targetFinalPos)) <= 0.1f + shipSizeAverage / 25.0f) {
                    slot.tryShoot();
                }
            }

            pointToRotate = targetFinalPos;

            if (bulletToShip > maxDistance)
                maxDistance = bulletToShip;

            if (targetToShip < minTargetToShip)
                minTargetToShip = targetToShip;
        }

        MathUtils.rotateToVector(ship, Objects.requireNonNullElse(pointToRotate, targetPos), ship.getEngine().getAngularVelocity());

        directionsToAdd.clear();
        if (minTargetToShip >= maxDistance - targetSizeAverage - shipSizeAverage) {
            List<Direction> dirs = MathUtils.calculateDirectionsToOtherObject(ship, targetPos.x, targetPos.y);

            for (int i = 0; i < dirs.size(); i++) {
                directionsToAdd.add(dirs.get(i));
            }

            sideDirection = null;
        } else if (distanceToTarget < maxDistance - targetSizeAverage - shipSizeAverage) {
            Direction dir = MathUtils.calculateDirectionToOtherObject(ship, targetPos.x, targetPos.y);
            if (dir == Direction.BACKWARD) {
                dir = Direction.FORWARD;
            } else if (dir == Direction.FORWARD) {
                dir = Direction.BACKWARD;
            } else if (dir == Direction.LEFT) {
                dir = Direction.RIGHT;
            } else if (dir == Direction.RIGHT) {
                dir = Direction.LEFT;
            }

            directionsToAdd.add(dir);

            if (changeDirTimer > 0 && sideDirection != null) {
                changeDirTimer -= 1;
            } else {
                Random rand = ship.getWorld().getRand();
                if (rand.nextInt(2) == 0) {
                    sideDirection = Direction.LEFT;
                } else {
                    sideDirection = Direction.RIGHT;
                }

                changeDirTimer = (int) ((1 + rand.nextFloat()) * TimeUtils.UPDATES_PER_SECOND);
            }
        } else {
            if (changeDirTimer > 0 && sideDirection != null) {
                changeDirTimer -= 1;
            } else {
                Random rand = ship.getWorld().getRand();
                if (rand.nextInt(2) == 0) {
                    sideDirection = Direction.LEFT;
                } else {
                    sideDirection = Direction.RIGHT;
                }

                changeDirTimer = (int) ((0.1f + rand.nextFloat() * 0.4f) * TimeUtils.UPDATES_PER_SECOND);
            }
        }

        if (sideDirection != null) {
            directionsToAdd.add(sideDirection);
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