package net.bfsr.network.packet.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.Engine;
import net.bfsr.engine.geometry.GeometryUtils;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.engine.network.util.ByteBufUtils;
import org.jbox2d.dynamics.Fixture;
import org.locationtech.jts.geom.Polygon;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class PacketSyncDamage extends PacketScheduled {
    private DamageableRigidBody damageable;
    private int x, y, maxX, maxY;
    private Polygon polygon;
    private byte[] bytes;

    private int id;
    private ByteBuffer byteBuffer;
    private int width, height;
    private List<Fixture> fixtures;

    public PacketSyncDamage(DamageableRigidBody damageable, double timestamp) {
        super(timestamp);
        this.damageable = damageable;
        DamageMask damageMask = damageable.getDamageMask();
        x = damageMask.getX();
        y = damageMask.getY();
        maxX = damageMask.getMaxX();
        maxY = damageMask.getMaxY();
        polygon = (Polygon) damageable.getPolygon().copy();
        bytes = damageMask.copy();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(damageable.getId());
        ByteBufUtils.writePolygon(data, polygon);
        data.writeShort(x);
        data.writeShort(y);
        data.writeShort(maxX);
        data.writeShort(maxY);

        int maskWidth = damageable.getDamageMask().getWidth();
        int width = maxX - x + 1;
        for (int i = y; i <= maxY; i++) {
            data.writeBytes(bytes, i * maskWidth + x, width);
        }
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        id = data.readInt();
        polygon = ByteBufUtils.readPolygon(data);
        x = data.readShort();
        y = data.readShort();
        short maxX = data.readShort();
        short maxY = data.readShort();
        width = maxX - x + 1;
        height = maxY - y + 1;

        int size = width * height;
        byteBuffer = Engine.getRenderer().createByteBuffer(size);
        data.readBytes(byteBuffer);
        byteBuffer.position(0);
        fixtures = new ArrayList<>(32);

        GeometryUtils.decompose(polygon, polygon -> fixtures.add(new Fixture(polygon)));
    }
}