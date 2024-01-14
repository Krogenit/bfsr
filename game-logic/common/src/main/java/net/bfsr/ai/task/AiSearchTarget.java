package net.bfsr.ai.task;

import net.bfsr.ai.AiAggressiveType;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.world.World;
import org.joml.Vector2f;

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
            RigidBody<?> attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
                ship.setTarget(attacker);
            } else findNewTarget();
        } else if (type == AiAggressiveType.DEFEND) {
            RigidBody<?> attacker;
            attacker = ship.getLastAttacker();
            if (attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
                ship.setTarget(attacker);
            }
        } else if (type == AiAggressiveType.NOTHING) {
            if (ship.getVelocity().length() > 0.1f && ship.getModules().getEngines().isSomeEngineAlive()) {
                ship.move(Direction.STOP);
            }
            if (Math.abs(ship.getAngularVelocity()) > 0.01f) {
                ship.getBody().setAngularVelocity(ship.getAngularVelocity() * 0.99f);
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
            Vector2f position = ship.getPosition();
            if (this.ship != ship && isEnemy(ship) && Math.abs(position.x) < 1000 && Math.abs(position.y) < 1000) {
                float newDist = this.ship.getPosition().distance(position);
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
        RigidBody<?> obj = ship.getTarget();
        if (obj == null) return true;
        if (obj instanceof Ship) {
            return ((Ship) obj).isDestroying() || obj.isDead();
        } else
            return obj.isDead();
    }
}