package net.bfsr.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import earcut4j.Earcut;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.Damageable;
import net.bfsr.engine.Engine;
import net.bfsr.network.packet.PacketAdapter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.decompose.SweepLine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class PacketSyncDamage extends PacketAdapter {
    private Damageable damagable;
    private int x, y, maxX, maxY;
    private PathsD contours;
    private byte[] bytes;

    private int id;
    private ByteBuffer byteBuffer;
    private int width, height;
    private List<BodyFixture> fixtures;

    public PacketSyncDamage(Damageable damagable) {
        this.damagable = damagable;
        DamageMask damageMask = damagable.getMask();
        x = damageMask.getX();
        y = damageMask.getY();
        maxX = damageMask.getMaxX();
        maxY = damageMask.getMaxY();
        contours = new PathsD();

        PathsD contours = damagable.getContours();
        for (int i = 0; i < contours.size(); i++) {
            this.contours.add(contours.get(i));
        }

        bytes = damageMask.copy();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(damagable.getId());

        data.writeByte(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            PathD pathD = contours.get(i);
            data.writeShort(pathD.size());
            for (int i1 = 0; i1 < pathD.size(); i1++) {
                PointD point = pathD.get(i1);
                data.writeFloat((float) point.x);
                data.writeFloat((float) point.y);
            }
        }

        data.writeShort(x);
        data.writeShort(y);
        data.writeShort(maxX);
        data.writeShort(maxY);

        int width = maxX - x + 1;
        for (int i = y; i <= maxY; i++) {
            data.writeBytes(bytes, i * damagable.getMask().getHeight() + x, width);
        }
    }

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

        DamageSystem.decompose(contours, convex -> fixtures.add(new BodyFixture(convex)), new SweepLine(), new Earcut());
    }
}