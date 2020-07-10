package ru.krogenit.bfsr.collision;

import org.dyn4j.Listener;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;

import ru.krogenit.bfsr.entity.bullet.Bullet;
import ru.krogenit.bfsr.entity.ship.Ship;

public class ContactListener implements org.dyn4j.dynamics.contact.ContactListener, Listener {

	@Override
	public void sensed(ContactPoint point) {
		
	}

	@Override
	public boolean begin(ContactPoint contact) {
		Body body1 = contact.getBody1();
		Body body2 = contact.getBody2();
		
		Object userData = body1.getUserData();
		if(userData != null) {
			if(userData instanceof Bullet) {
				((Bullet)userData).checkCollision(contact, body2);
			} else if(userData instanceof Ship) {
				((Ship)userData).checkCollision(contact, body2);
			}
		}
		
		userData = body2.getUserData();
		if(userData != null) {
			if(userData instanceof Bullet) {
				((Bullet)userData).checkCollision(contact, body1);
			} else if(userData instanceof Ship) {
				((Ship)userData).checkCollision(contact, body1);
			}
		}

		return true;
	}

	@Override
	public void end(ContactPoint point) {

	}

	@Override
	public boolean persist(PersistedContactPoint point) {

		return true;
	}

	@Override
	public boolean preSolve(ContactPoint point) {

		return true;
	}

	@Override
	public void postSolve(SolvedContactPoint point) {

		
	}

}
