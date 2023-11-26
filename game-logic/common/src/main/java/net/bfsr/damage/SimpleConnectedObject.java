package net.bfsr.damage;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.util.ByteBufUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SimpleConnectedObject extends GameObject implements ConnectedObject {
    private GameObjectConfigData configData;
    private List<BodyFixture> fixtures;

    @Override
    public void spawn() {}

    public void setupFixtures(RigidBody<?> rigidBody) {
        for (int i = 0; i < fixtures.size(); i++) {
            rigidBody.setupFixture(fixtures.get(i));
        }
    }

    @Override
    public void addFixtures(Body body) {
        for (int i = 0; i < fixtures.size(); i++) {
            body.addFixture(fixtures.get(i));
        }
    }

    @Override
    public void writeData(ByteBuf data) {}

    @Override
    public void readData(ByteBuf data) {
        configData = ((GameObjectConfigData) ConfigConverterManager.INSTANCE.getConverter(data.readInt()).get(data.readInt()));
        ByteBufUtils.readVector(data, position);
        setSize(configData.getSizeX(), configData.getSizeY());

        List<Convex> convexList = configData.getConvexList();
        fixtures = new ArrayList<>(convexList.size());
        for (int i1 = 0; i1 < convexList.size(); i1++) {
            Convex convex = convexList.get(i1);
            if (convex instanceof Polygon polygonConvex) {
                Polygon polygon = Geometry.createPolygon(polygonConvex.getVertices());
                polygon.translate(position.x, position.y);
                fixtures.add(new BodyFixture(polygon));
            }
        }
    }

    @Override
    public float getConnectPointX() {
        return position.x;
    }

    @Override
    public float getConnectPointY() {
        return position.y;
    }
}