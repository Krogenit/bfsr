package net.bfsr.damage;

import io.netty.buffer.ByteBuf;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.Body;
import org.joml.Vector2f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

public interface ConnectedObject<CONFIG_DATA extends GameObjectConfigData> {
    void init(RigidBody<?> rigidBody);
    void spawn();
    void update();
    void postPhysicsUpdate(RigidBody<?> rigidBody);
    default boolean isInside(Polygon polygon) {
        return polygon.contains(
                DamageSystem.GEOMETRY_FACTORY.createPoint(new Coordinate(getConnectPointX(), getConnectPointY())));
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