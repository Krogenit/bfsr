package net.bfsr.ai.task;

import net.bfsr.ai.AiAggressiveType;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.world.World;

import java.util.List;

public class AiSearchTarget extends AiTask {
    private final float maxSearchRange;

    public AiSearchTarget(Ship ship, float searchRange) {
        super(ship);
        this.maxSearchRange = searchRange;
    }

    @Override
    public void execute() {
        AiAggressiveType type = ship.getAi().getAggressiveType();
        switch (type) {
            case ATTACK:
                CollisionObject attacker = ship.getLastAttacker();
                if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
                    ship.setTarget(attacker);
                } else findNewTarget();
                break;
            case DEFEND:
                attacker = ship.getLastAttacker();
                if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
                    ship.setTarget(attacker);
                }
                break;
            case NOTHING:
                break;
        }
    }

    private void findNewTarget() {
        World world = ship.getWorld();
        List<Ship> ships = world.getShips();
        float distance = Float.MAX_VALUE;
        Ship nearShip = null;
        for (Ship ship : ships) {
            if (this.ship != ship && isEnemy(ship)) {
                float newDist = this.ship.getPosition().distance(ship.getPosition());
                if (newDist < distance && newDist <= maxSearchRange) {
                    nearShip = ship;
                    distance = newDist;
                }
            }
        }

        if (nearShip != null) {
            ship.setTarget(nearShip);
        }
    }

    private boolean isEnemy(Ship ship) {
        return this.ship.getFaction() != ship.getFaction();
    }

    @Override
    public boolean shouldExecute() {
        CollisionObject obj = ship.getTarget();
        if (obj == null) return true;
        if (obj instanceof Ship) {
            return ((Ship) obj).isDestroing() || obj.isDead();
        } else
            return obj.isDead();
    }
}
