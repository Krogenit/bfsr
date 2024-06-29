package net.bfsr.physics;

import gnu.trove.set.hash.THashSet;
import net.bfsr.entity.RigidBody;
import org.dyn4j.collision.continuous.TimeOfImpact;
import org.dyn4j.dynamics.Body;
import org.dyn4j.world.listener.TimeOfImpactListenerAdapter;

public class CCDTransformHandler extends TimeOfImpactListenerAdapter<Body> {
    private final THashSet<RigidBody> affectedEntities = new THashSet<>();

    @Override
    public boolean collision(Body body1, Body body2, TimeOfImpact toi) {
        if (!body1.isBullet()) saveTransform(((RigidBody) body1.getUserData()), body1);
        if (!body2.isBullet()) saveTransform(((RigidBody) body2.getUserData()), body2);
        return true;
    }

    private void saveTransform(RigidBody rigidBody, Body body) {
        if (affectedEntities.add(rigidBody)) {
            rigidBody.saveTransform(body.getTransform());
        }
    }

    public void restoreTransforms() {
        affectedEntities.forEach(gameObject -> {
            gameObject.restoreTransform();
            return true;
        });
    }

    public void clear() {
        affectedEntities.clear();
    }
}