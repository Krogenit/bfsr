package net.bfsr.damage;

import io.netty.buffer.ByteBuf;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import org.jbox2d.dynamics.Body;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

public interface ConnectedObject<CONFIG_DATA extends GameObjectConfigData> {
    void init(RigidBody rigidBody);
    void spawn();
    void update();
    void postPhysicsUpdate(RigidBody rigidBody);
    default boolean isInside(Polygon polygon) {
        return polygon.contains(
                DamageSystem.GEOMETRY_FACTORY.createPoint(new Coordinate(getConnectPointX(), getConnectPointY())));
    }
    float getConnectPointX();
    float getConnectPointY();
    void writeData(ByteBuf data);
    void addFixtures(Body body);
    ConnectedObjectType getConnectedObjectType();
    int getRegistryId();
    int getDataId();
    CONFIG_DATA getConfigData();
    float getX();
    float getY();
    float getSizeX();
    float getSizeY();
    float getSin();
    float getCos();
}