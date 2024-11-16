package net.bfsr.physics.filter;

import lombok.RequiredArgsConstructor;
import net.bfsr.entity.RigidBody;
import net.bfsr.physics.CollisionMatrix;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;

@RequiredArgsConstructor
public class ContactFilter extends org.jbox2d.callbacks.ContactFilter {
    private final CollisionMatrix collisionMatrix;

    @Override
    public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
        Filter filterA = fixtureA.getFilter();
        Filter filterB = fixtureB.getFilter();

        return (filterA.maskBits & filterB.categoryBits) != 0 && (filterA.categoryBits & filterB.maskBits) != 0 &&
                collisionMatrix.canCollideWith((RigidBody) fixtureA.body.userData, (RigidBody) fixtureB.body.userData);
    }

    public boolean shouldCollide(Filter filterA, Filter filterB) {
        return (filterA.maskBits & filterB.categoryBits) != 0 && (filterA.categoryBits & filterB.maskBits) != 0;
    }
}
