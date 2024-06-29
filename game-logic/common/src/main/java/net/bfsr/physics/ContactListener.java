package net.bfsr.physics;

import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

public class ContactListener extends ContactListenerAdapter<Body> {
    private final CollisionMatrix collisionMatrix;

    public ContactListener(CollisionMatrix collisionMatrix) {
        this.collisionMatrix = collisionMatrix;
    }

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

        RigidBody rigidBody1 = (RigidBody) body1.getUserData();
        RigidBody rigidBody2 = (RigidBody) body2.getUserData();
        collisionMatrix.collision(rigidBody1, rigidBody2, collision.getFixture1(), collision.getFixture2(), pointX, pointY,
                normalX, normalY, collision);
    }
}