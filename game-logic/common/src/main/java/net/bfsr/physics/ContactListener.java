package net.bfsr.physics;

import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

public class ContactListener extends ContactListenerAdapter<Body> {
    @Override
    public void collision(ContactCollisionData<Body> collision) {
        Body body1 = collision.getBody1();
        Body body2 = collision.getBody2();
        Vector2 point = collision.getContactConstraint().getContacts().get(0).getPoint();
        float pointX = (float) point.x;
        float pointY = (float) point.y;
        Vector2 normal = collision.getPenetration().getNormal();
        float normalX = (float) normal.x;
        float normalY = (float) normal.y;

        Object userData = body1.getUserData();
        if (userData instanceof RigidBody<?> rigidBody) {
            rigidBody.collision(body2, collision.getFixture2(), pointX, pointY, -normalX, -normalY, collision);
        }

        userData = body2.getUserData();
        if (userData instanceof RigidBody<?> rigidBody) {
            rigidBody.collision(body1, collision.getFixture1(), pointX, pointY, normalX, normalY, collision);
        }
    }
}