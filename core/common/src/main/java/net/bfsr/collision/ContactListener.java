package net.bfsr.collision;

import net.bfsr.entity.GameObject;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.SolvedContact;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

public class ContactListener extends ContactListenerAdapter<Body> {
    @Override
    public void postSolve(ContactCollisionData<Body> collision, SolvedContact contact) {
        Vector2 normal = collision.getContactConstraint().getNormal();
        Body body1 = collision.getBody1();
        Body body2 = collision.getBody2();

        Object userData = body1.getUserData();
        if (userData instanceof GameObject gameObject) {
            gameObject.checkCollision(contact, normal, body2);
        }

        userData = body2.getUserData();
        if (userData instanceof GameObject gameObject) {
            gameObject.checkCollision(contact, normal, body1);
        }
    }
}