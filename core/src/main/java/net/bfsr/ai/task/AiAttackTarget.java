package net.bfsr.ai.task;

import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.util.TimeUtils;
import org.joml.Vector2f;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AiAttackTarget extends AiTask {
    private final float maxAttackRange;
    private float changeDirTimer;
    private Direction curDir;

    public AiAttackTarget(Ship ship, float maxAttackRange) {
        super(ship);
        this.maxAttackRange = maxAttackRange;
    }

    @Override
    public void execute() {
        CollisionObject target = ship.getTarget();
        Vector2f targetPos = target.getPosition();
        Vector2f pos = ship.getPosition();

        if (targetPos.distance(pos) >= maxAttackRange || target.isDead()) {
            ship.setTarget(null);
            return;
        }

        float distanceToTarget = targetPos.distance(pos);

        Vector2f shipSize = ship.getScale();
        float shipSizeAverage = (shipSize.x + shipSize.y) / 2.0f;
        float minTargetToShip = Float.MAX_VALUE;

        float maxDistance = 0;
        Vector2f pointToRotate = null;
        Vector2f targetSize = target.getScale();
        float targetSizeAverage = (targetSize.x + targetSize.y) / 2.0f;

        Vector2f targetVelocity = target.getVelocity();

        List<WeaponSlot> slots = ship.getWeaponSlots();
        int size = slots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = slots.get(i);
            if (slot != null) {
                float bulletToShip;
                Vector2f targetFinalPos;

                if (slot instanceof WeaponSlotBeam) {
                    bulletToShip = ((WeaponSlotBeam) slot).getBeamMaxRange();
                    targetFinalPos = new Vector2f(targetPos.x + 0, targetPos.y + 0);
                } else {
                    Vector2f slotPos = slot.getAddPosition();
                    float x1 = ship.getCos();
                    float y1 = ship.getSin();
                    float xPos = x1 * slotPos.x - y1 * slotPos.y;
                    float yPos = y1 * slotPos.x + x1 * slotPos.y;

                    float bulletSpeed = slot.getBulletSpeed();
                    int totalIterations = (int) (1.5f / (slot.getAlphaReducer() * TimeUtils.UPDATE_DELTA_TIME));

                    Vector2f totalVelocity = new Vector2f(-x1, -y1).mul(bulletSpeed * TimeUtils.UPDATE_DELTA_TIME).mul(totalIterations);
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
                    if (Math.abs(ship.getRotationDifference(targetFinalPos)) <= 0.1f + shipSizeAverage / 25.0f) {
                        slot.shoot();
                    }
                }

                pointToRotate = targetFinalPos;

                if (bulletToShip > maxDistance)
                    maxDistance = bulletToShip;

                if (targetToShip < minTargetToShip)
                    minTargetToShip = targetToShip;
            }
        }

        ship.rotateToVector(Objects.requireNonNullElse(pointToRotate, targetPos), ship.getEngine().getRotationSpeed());

        Direction[] dirs = ship.calculateDirectionsToOtherObject(targetPos.x, targetPos.y);
        if (minTargetToShip >= maxDistance) {
            if (dirs[0] != null) ship.move(ship, dirs[0]);
            if (dirs[1] != null) ship.move(ship, dirs[1]);
        } else if (distanceToTarget < maxDistance - targetSizeAverage - shipSizeAverage) {
            Direction dir = ship.calculateDirectionToOtherObject(targetPos.x, targetPos.y);
            if (dir == Direction.BACKWARD) {
                dir = Direction.FORWARD;
            } else if (dir == Direction.FORWARD) {
                dir = Direction.BACKWARD;
            } else if (dir == Direction.LEFT) {
                dir = Direction.RIGHT;
            } else if (dir == Direction.RIGHT) {
                dir = Direction.LEFT;
            }
            ship.move(ship, dir);
            if (curDir != null && changeDirTimer > 0) {
                changeDirTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                ship.move(ship, curDir);
            } else {
                Random rand = ship.getWorld().getRand();
                if (rand.nextInt(2) == 0)
                    curDir = Direction.LEFT;
                else
                    curDir = Direction.RIGHT;
                changeDirTimer = 60 + rand.nextInt(60);
            }
        } else {
            if (curDir != null && changeDirTimer > 0) {
                changeDirTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                ship.move(ship, curDir);
            } else {
                Random rand = ship.getWorld().getRand();
                if (rand.nextInt(2) == 0)
                    curDir = Direction.LEFT;
                else
                    curDir = Direction.RIGHT;
                changeDirTimer = 10 + rand.nextInt(40);
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        return ship.getTarget() != null && !ship.getTarget().isDead();
    }
}
