package net.bfsr.server.ai.task;

import net.bfsr.server.ai.AiAggressiveType;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.world.WorldServer;

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
        if (type == AiAggressiveType.ATTACK) {
            CollisionObject attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
                ship.setTarget(attacker);
            } else findNewTarget();
        } else if (type == AiAggressiveType.DEFEND) {
            CollisionObject attacker;
            attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
                ship.setTarget(attacker);
            }
        }
    }

    private void findNewTarget() {
        WorldServer world = ship.getWorld();
        List<Ship> ships = world.getShips();
        float distance = Float.MAX_VALUE;
        Ship nearShip = null;
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            Ship ship = ships.get(i);
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
            return ((Ship) obj).isDestroying() || obj.isDead();
        } else
            return obj.isDead();
    }
}