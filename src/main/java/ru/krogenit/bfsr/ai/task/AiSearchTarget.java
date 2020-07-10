package ru.krogenit.bfsr.ai.task;

import java.util.List;

import ru.krogenit.bfsr.ai.AiAggressiveType;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.world.World;

public class AiSearchTarget extends AiTask {

	float maxSearchRange;
	
	public AiSearchTarget(Ship ship, float searchRange) {
		super(ship);
		this.maxSearchRange = searchRange;
	}

	@Override
	public void execute(double delta) {
		AiAggressiveType type = ship.getAi().getAggressiveType();
		switch(type) {
		case Attack:
			CollisionObject attacker = ship.getLastAttacker();
			if(attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
				ship.setTarget(attacker);
			} else findNewTarget();
			break;
		case Defend:
			attacker = ship.getLastAttacker();
			if(attacker != null && !attacker.isDead() && attacker instanceof Ship && isEnemy((Ship) attacker)) {
				ship.setTarget(attacker);
			}
			break;
		case Nothing:
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
		
		if(nearShip != null) {
			ship.setTarget(nearShip);
		}
	}
	
	private boolean isEnemy(Ship ship) {
		return this.ship.getFaction() != ship.getFaction() ;
	}
	
	@Override
	public boolean shouldExecute() {
		CollisionObject obj = ship.getTarget();
		if(obj == null) return true;
		if(obj instanceof Ship) {
			return ((Ship) obj).isDestroing() || obj.isDead();
		} else 
			return obj.isDead();
	}
}
