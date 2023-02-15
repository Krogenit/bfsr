package net.bfsr.server.ai.task;

import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.server.ai.AiAggressiveType;
import net.bfsr.server.entity.Ship;
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
        if (type == AiAggressiveType.ATTACK) {
            CollisionObject attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof ShipCommon && isEnemy((ShipCommon) attacker)) {
                ship.setTarget(attacker);
            } else findNewTarget();
        } else if (type == AiAggressiveType.DEFEND) {
            CollisionObject attacker;
            attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof ShipCommon && isEnemy((ShipCommon) attacker)) {
                ship.setTarget(attacker);
            }
        } else if (type == AiAggressiveType.NOTHING) {
        }
    }

    private void findNewTarget() {
        World world = ship.getWorld();
        List<ShipCommon> ships = world.getShips();
        float distance = Float.MAX_VALUE;
        ShipCommon nearShip = null;
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            ShipCommon ship = ships.get(i);
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

    private boolean isEnemy(ShipCommon ship) {
        return this.ship.getFaction() != ship.getFaction();
    }

    @Override
    public boolean shouldExecute() {
        CollisionObject obj = ship.getTarget();
        if (obj == null) return true;
        if (obj instanceof ShipCommon) {
            return ((ShipCommon) obj).isDestroying() || obj.isDead();
        } else
            return obj.isDead();
    }
}
