package net.bfsr.server.ai.task;

import lombok.RequiredArgsConstructor;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.ai.task.AiTask;
import net.bfsr.engine.entity.RigidBody;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;

import java.util.List;

@RequiredArgsConstructor
public class AiSearchTarget extends AiTask {
    private final float maxSearchRange;

    @Override
    public void execute() {
        AiAggressiveType type = ship.getAi().getAggressiveType();
        if (type == AiAggressiveType.ATTACK) {
            RigidBody attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker) &&
                    Math.abs(attacker.getX()) < 1000 && Math.abs(attacker.getY()) < 1000) {
                ship.setTarget(attacker);
            } else findNewTarget();
        } else if (type == AiAggressiveType.DEFEND) {
            RigidBody attacker;
            attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
                ship.setTarget(attacker);
            }
        } else if (type == AiAggressiveType.NOTHING) {
            if (ship.getLinearVelocity().length() > 0.1f && ship.getModules().getEngines().isSomeEngineAlive()) {
                ship.move(Direction.STOP);
            }
            if (Math.abs(ship.getAngularVelocity()) > 0.01f) {
                ship.setAngularVelocity(ship.getAngularVelocity() * 0.99f);
            }
        }
    }

    private void findNewTarget() {
        World world = ship.getWorld();
        List<Ship> ships = world.getEntitiesByType(Ship.class);
        float distance = Float.MAX_VALUE;
        Ship nearShip = null;
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            Ship ship = ships.get(i);
            if (this.ship != ship && isEnemy(ship) && Math.abs(ship.getX()) < 1000 && Math.abs(ship.getY()) < 1000) {
                float newDist = this.ship.getBody().getTransform().position.distance(ship.getX(), ship.getY());
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
        RigidBody obj = ship.getTarget();
        if (obj == null) return true;
        if (obj instanceof Ship) {
            return ((Ship) obj).isDestroying() || obj.isDead();
        } else
            return obj.isDead();
    }
}