package net.bfsr.client.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import earcut4j.Earcut;
import io.netty.buffer.ByteBuf;
import net.bfsr.client.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.damage.DamageUtils;
import net.bfsr.damage.Damageable;
import net.bfsr.engine.Engine;
import net.bfsr.entity.GameObject;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.decompose.SweepLine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PacketSyncDamage implements PacketIn {
    private int id;
    private PathsD contours;
    private ByteBuffer byteBuffer;
    private int x, y, width, height;
    private List<BodyFixture> fixtures;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        byte contoursCount = data.readByte();
        contours = new PathsD(contoursCount);
        for (int i = 0; i < contoursCount; i++) {
            short pathSize = data.readShort();
            PathD pathD = new PathD(pathSize);
            for (int j = 0; j < pathSize; j++) {
                pathD.add(new PointD(data.readFloat(), data.readFloat()));
            }
            contours.add(pathD);
        }

        x = data.readShort();
        y = data.readShort();
        short maxX = data.readShort();
        short maxY = data.readShort();
        width = maxX - x + 1;
        height = maxY - y + 1;

        int size = width * height;
        byteBuffer = Engine.renderer.createByteBuffer(size);
        byte[] bytes = new byte[size];
        data.readBytes(bytes, 0, size);
        byteBuffer.put(bytes, 0, size);
        byteBuffer.flip();
        fixtures = new ArrayList<>(32);

        DamageUtils.decompose(contours, convex -> fixtures.add(new BodyFixture(convex)), new SweepLine(), new Earcut());
    }

    @Override
    public void processOnClientSide() {
        GameObject gameObject = Core.get().getWorld().getEntityById(id);
        if (gameObject instanceof Damageable damageable) {
            damageable.setContours(contours);
            if (fixtures.size() > 0) {
                damageable.setFixtures(fixtures);
            }

            DamageHandler.updateDamage(damageable, x, y, width, height, byteBuffer);
        }
    }
}