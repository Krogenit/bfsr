package ru.krogenit.bfsr.collision;

import org.dyn4j.Listener;
import org.dyn4j.collision.manifold.Manifold;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.contact.ContactConstraint;

import ru.krogenit.bfsr.entity.bullet.Bullet;
import ru.krogenit.bfsr.entity.ship.Ship;

public class CollisionListener implements org.dyn4j.dynamics.CollisionListener, Listener {

	@Override
	public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2) {
		return true;
	}

	@Override
	public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {
		return true;
	}

	@Override
	public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Manifold manifold) {
		return true;
	}

	@Override
	public boolean collision(ContactConstraint contact) {
		
		return true;
	}

}
