package net.bfsr.physics;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.Objects;

@RequiredArgsConstructor
public class ContactListener implements org.jbox2d.callbacks.ContactListener {
    private final CollisionMatrix collisionMatrix;

    private final Vector2 worldPoint = new Vector2();
    private final Vector2 worldNormal = new Vector2();

    private final Object2ObjectMap<Pair, Contact> contactMap = new Object2ObjectOpenHashMap<>();
    private final ObjectPool<Pair> pairPool = new ObjectPool<>(Pair::new);

    private static class Pair {
        private RigidBody rigidBody1;
        private RigidBody rigidBody2;

        void set(RigidBody rigidBody1, RigidBody rigidBody2) {
            this.rigidBody1 = rigidBody1;
            this.rigidBody2 = rigidBody2;
        }

        @Override
        public boolean equals(Object obj) {
            return Objects.equals((rigidBody1), ((Pair) obj).rigidBody1) &&
                    Objects.equals((rigidBody2), ((Pair) obj).rigidBody2);
        }

        @Override
        public int hashCode() {
            return rigidBody1.hashCode() * 19 + rigidBody2.hashCode();
        }
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        Body body1 = fixtureA.getBody();
        Body body2 = fixtureB.getBody();
        RigidBody rigidBody1 = (RigidBody) body1.getUserData();
        RigidBody rigidBody2 = (RigidBody) body2.getUserData();

        Pair pair = pairPool.get();
        pair.set(rigidBody1, rigidBody2);
        if (contactMap.containsKey(pair)) {
            pairPool.returnBack(pair);
            return;
        }

        Manifold manifold = contact.manifold;
        Manifold.ManifoldType type = manifold.type;

        if (type == Manifold.ManifoldType.FACE_A) {
            Rotation.mulToOutUnsafe(body1.getTransform().rotation, manifold.localNormal, worldNormal);
            Transform.mulToOut(body1.getTransform(), manifold.localPoint, worldPoint);
        } else {
            Transform.mulToOut(body2.getTransform(), manifold.localPoint, worldPoint);
            Rotation.mulToOutUnsafe(body2.getTransform().rotation, manifold.localNormal, worldNormal);
        }

        collisionMatrix.collision(rigidBody1, rigidBody2, fixtureA, fixtureB, worldPoint.x, worldPoint.y, worldNormal.x, worldNormal.y);
        contactMap.put(pair, contact);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        Body body1 = fixtureA.getBody();
        Body body2 = fixtureB.getBody();
        RigidBody rigidBody1 = (RigidBody) body1.getUserData();
        RigidBody rigidBody2 = (RigidBody) body2.getUserData();

        Pair pair = pairPool.get();
        pair.set(rigidBody1, rigidBody2);
        contactMap.remove(pair);
        pairPool.returnBack(pair);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Bullet ||
                contact.getFixtureB().getBody().getUserData() instanceof Bullet) {
            contact.setEnabled(false);
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}