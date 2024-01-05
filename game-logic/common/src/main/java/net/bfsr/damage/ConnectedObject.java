package net.bfsr.damage;

import clipper2.core.InternalClipper;
import clipper2.core.PathD;
import io.netty.buffer.ByteBuf;
import org.dyn4j.dynamics.Body;

public interface ConnectedObject {
    void spawn();
    default boolean isInside(PathD contour) {
        return InternalClipper.PointInPolygonOptimized(getConnectPointX(), getConnectPointY(), contour);
    }
    float getConnectPointX();
    float getConnectPointY();
    void writeData(ByteBuf data);
    void readData(ByteBuf data);
    void addFixtures(Body body);
}