package net.bfsr.collision;

import gnu.trove.set.hash.THashSet;
import net.bfsr.entity.GameObject;
import org.dyn4j.collision.continuous.TimeOfImpact;
import org.dyn4j.dynamics.Body;
import org.dyn4j.world.listener.TimeOfImpactListenerAdapter;

public class CCDTransformHandler extends TimeOfImpactListenerAdapter<Body> {
    private final THashSet<GameObject> affectedBodies = new THashSet<>();

    @Override
    public boolean collision(Body body1, Body body2, TimeOfImpact toi) {
        saveTransform(((GameObject) body1.getUserData()), body1);
        saveTransform(((GameObject) body2.getUserData()), body2);
        return true;
    }

    private void saveTransform(GameObject gameObject, Body body) {
        if (affectedBodies.add(gameObject)) {
            gameObject.saveTransform(body.getTransform());
        }
    }

    public void restoreTransforms() {
        affectedBodies.forEach(gameObject -> {
            gameObject.restoreTransform();
            return true;
        });
    }

    public void clear() {
        affectedBodies.clear();
    }
}