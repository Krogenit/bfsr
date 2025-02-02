package net.bfsr.damage;

import io.netty.buffer.ByteBuf;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

public interface ConnectedObject<CONFIG_DATA extends GameObjectConfigData> {
    void init(RigidBody rigidBody);
    void spawn();
    void update();
    void postPhysicsUpdate(RigidBody rigidBody);
    default boolean isInside(Polygon polygon) {
        return isInside(polygon, 0, 0);
    }
    default boolean isInside(Polygon polygon, float offsetX, float offsetY) {
        return polygon.contains(
                DamageSystem.GEOMETRY_FACTORY.createPoint(new Coordinate(getConnectPointX() + offsetX, getConnectPointY() + offsetY)));
    }
    void addPositionOffset(float x, float y);
    float getConnectPointX();
    float getConnectPointY();
    void writeData(ByteBuf data);
    void addFixtures(DamageableRigidBody rigidBody);
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