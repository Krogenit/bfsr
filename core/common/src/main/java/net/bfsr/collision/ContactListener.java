package net.bfsr.collision;

import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.entity.ship.ShipCommon;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

public class ContactListener extends ContactListenerAdapter<Body> {
    @Override
    public void begin(ContactCollisionData<Body> collision, Contact contact) {
        Body body1 = collision.getBody1();
        Body body2 = collision.getBody2();

        Object userData = body1.getUserData();
        if (userData != null) {
            if (userData instanceof BulletCommon) {
                ((BulletCommon) userData).checkCollision(contact, collision.getContactConstraint().getNormal(), body2);
            } else if (userData instanceof ShipCommon) {
                ((ShipCommon) userData).checkCollision(contact, collision.getContactConstraint().getNormal(), body2);
            }
        }

        userData = body2.getUserData();
        if (userData != null) {
            if (userData instanceof BulletCommon) {
                ((BulletCommon) userData).checkCollision(contact, collision.getContactConstraint().getNormal(), body1);
            } else if (userData instanceof ShipCommon) {
                ((ShipCommon) userData).checkCollision(contact, collision.getContactConstraint().getNormal(), body1);
            }
        }
    }
}
