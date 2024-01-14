package net.bfsr.damage;

import clipper2.core.InternalClipper;
import clipper2.core.PathD;
import io.netty.buffer.ByteBuf;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.Body;
import org.joml.Vector2f;

public interface ConnectedObject<CONFIG_DATA extends GameObjectConfigData> {
    void init(RigidBody<?> rigidBody);
    void spawn();
    void update();
    void postPhysicsUpdate(RigidBody<?> rigidBody);
    default boolean isInside(PathD contour) {
        return InternalClipper.PointInPolygonOptimized(getConnectPointX(), getConnectPointY(), contour);
    }
    float getConnectPointX();
    float getConnectPointY();
    void writeData(ByteBuf data);
    void readData(ByteBuf data);
    void addFixtures(Body body);
    ConnectedObjectType getConnectedObjectType();
    int getRegistryId();
    int getDataId();
    CONFIG_DATA getConfigData();
    Vector2f getPosition();
    Vector2f getSize();
    float getSin();
    float getCos();
}