package net.bfsr.engine.physics.collision;

import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.dynamics.Fixture;

import java.util.Arrays;

public class AbstractCollisionMatrix {
    @SuppressWarnings("rawtypes")
    private final CollisionListener[][] matrix;
    @SuppressWarnings("rawtypes")
    private final RayCastListener[][] rayCastMatrix;
    @SuppressWarnings("rawtypes")
    private final CanCollideFunction[][] canCollideFunctions;

    public AbstractCollisionMatrix(int size) {
        canCollideFunctions = new CanCollideFunction[size][size];
        matrix = new CollisionListener[size][size];
        rayCastMatrix = new RayCastListener[size][size];
        CanCollideFunction<?, ?> canCollideFunction = (rigidBody1, rigidBody2) -> true;
        CollisionListener<?, ?> listener = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX, normalY) -> {};
        RayCastListener<?, ?> rayCastListener = (rayCastSource, rigidBody, fixture, contactX, contactY, normalX, normalY) -> {};
        for (int i = 0; i < canCollideFunctions.length; i++) {
            Arrays.fill(canCollideFunctions[i], canCollideFunction);
        }
        for (int i = 0; i < matrix.length; i++) {
            Arrays.fill(matrix[i], listener);
        }
        for (int i = 0; i < rayCastMatrix.length; i++) {
            Arrays.fill(rayCastMatrix[i], rayCastListener);
        }
    }

    @SuppressWarnings("unchecked")
    protected void register(int entityTypeId1, int entityTypeId2, @SuppressWarnings("rawtypes") CanCollideFunction canCollideFunction) {
        canCollideFunctions[entityTypeId1][entityTypeId2] = canCollideFunction;
        canCollideFunctions[entityTypeId2][entityTypeId1] = (rigidBody1, rigidBody2) -> canCollideFunction.apply(rigidBody2, rigidBody1);
    }

    @SuppressWarnings("unchecked")
    protected void register(int entityTypeId1, int entityTypeId2, @SuppressWarnings("rawtypes") CollisionListener collisionListener) {
        matrix[entityTypeId1][entityTypeId2] = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX, normalY) ->
                collisionListener.handle(rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, -normalX, -normalY);
        matrix[entityTypeId2][entityTypeId1] = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX, normalY) ->
                collisionListener.handle(rigidBody2, rigidBody1, fixture2, fixture1, contactX, contactY, normalX, normalY);
    }

    protected void register(int rayCastTypeId1, int entityTypeId2, @SuppressWarnings("rawtypes") RayCastListener rayCastListener) {
        rayCastMatrix[rayCastTypeId1][entityTypeId2] = rayCastListener;
    }

    @SuppressWarnings("unchecked")
    void collision(RigidBody rigidBody1, RigidBody rigidBody2, Fixture fixture1, Fixture fixture2,
                   float contactX, float contactY, float normalX, float normalY) {
        matrix[rigidBody1.getCollisionMatrixId()][rigidBody2.getCollisionMatrixId()].handle(rigidBody1, rigidBody2,
                fixture1, fixture2, contactX, contactY, normalX, normalY);
    }

    @SuppressWarnings("unchecked")
    public void rayCast(RayCastSource rayCastSource, Fixture fixture, float contactX, float contactY,
                        float normalX, float normalY) {
        RigidBody rigidBody = ((RigidBody) fixture.getBody().getUserData());
        rayCastMatrix[rayCastSource.getRayCastType()][rigidBody.getCollisionMatrixId()].handle(rayCastSource, rigidBody,
                fixture, contactX, contactY, normalX, normalY);
    }

    @SuppressWarnings("unchecked")
    public boolean canCollideWith(RigidBody rigidBody1, RigidBody rigidBody2) {
        return canCollideFunctions[rigidBody1.getCollisionMatrixId()][rigidBody2.getCollisionMatrixId()].apply(rigidBody1,
                rigidBody2);
    }

    @FunctionalInterface
    protected interface CollisionListener<BODY_1 extends RigidBody, BODY_2 extends RigidBody> {
        void handle(BODY_1 rigidBody1, BODY_2 rigidBody2, Fixture fixture1, Fixture fixture2,
                    float contactX, float contactY, float normalX, float normalY);
    }

    @FunctionalInterface
    protected interface RayCastListener<RAY_CAST_SOURCE extends RayCastSource, RIGID_BODY extends RigidBody> {
        void handle(RAY_CAST_SOURCE rayCastSource, RIGID_BODY rigidBody, Fixture fixture, float contactX, float contactY,
                    float normalX, float normalY);
    }

    @FunctionalInterface
    protected interface CanCollideFunction<BODY_1 extends RigidBody, BODY_2 extends RigidBody> {
        boolean apply(BODY_1 rigidBody1, BODY_2 rigidBody2);
    }
}