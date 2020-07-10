package ru.krogenit.bfsr.collision.filter;

import org.dyn4j.collision.Filter;

import ru.krogenit.bfsr.client.particle.ParticleWreck;
import ru.krogenit.bfsr.entity.bullet.Bullet;
import ru.krogenit.bfsr.entity.ship.Ship;

public class WreckFilter extends CollisionFilter {

	public WreckFilter(Object userData) {
		super(userData);
	}
	
	@Override
	public boolean isAllowed(Filter filter) {
		if(filter == null) return false;
		
		if(filter instanceof CollisionFilter) {
			Object otherData = ((CollisionFilter) filter).getUserData();
			return otherData instanceof Ship || otherData instanceof ParticleWreck || otherData instanceof Bullet;
		}
		
		return false;
	}

}
