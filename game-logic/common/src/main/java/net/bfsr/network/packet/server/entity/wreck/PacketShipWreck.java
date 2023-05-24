package net.bfsr.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PointD;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageUtils;
import net.bfsr.damage.Damageable;
import net.bfsr.engine.Engine;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.network.packet.PacketAdapter;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.io.IOException;
import java.nio.ByteBuffer;

@NoArgsConstructor
@Getter
public class PacketShipWreck extends PacketAdapter {
    private ShipWreck wreck;
    private int x, y;
    private int id;
    private float posX, posY;
    private float sin, cos;
    private float sizeX, sizeY;
    private short dataIndex;
    private float velocityX, velocityY;
    private float angularVelocity;
    private PathD pathD;
    private int width, height;
    private ByteBuffer byteBuffer;

    private Damageable damageable;
    private int maxX, maxY;
    private PathD path;

    public PacketShipWreck(Damageable damageable) {
        this.damageable = damageable;
        DamageMask damageMask = damageable.getMask();
        x = damageMask.getX();
        y = damageMask.getY();
        maxX = damageMask.getMaxX();
        maxY = damageMask.getMaxY();
        path = damageable.getContours().get(0);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(damageable.getId());
        data.writeFloat(damageable.getX());
        data.writeFloat(damageable.getY());
        data.writeFloat(damageable.getSin());
        data.writeFloat(damageable.getCos());
        Vector2f scale = damageable.getSize();
        data.writeFloat(scale.x);
        data.writeFloat(scale.y);
        data.writeShort(damageable.getDataIndex());
        DamageMask damageMask = damageable.getMask();
        data.writeShort(x);
        data.writeShort(y);
        data.writeShort(maxX);
        data.writeShort(maxY);
        byte[] bytes = damageMask.getData();
        int width = maxX - x + 1;
        try {
            for (int i = y; i <= maxY; i++) {
                data.writeBytes(bytes, i * damageMask.getHeight() + x, width);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("width " + width + " height " + (maxY - y + 1) + " buffer size " + bytes.length);
            e.printStackTrace();
        }

        data.writeShort(path.size());
        for (int i = 0; i < path.size(); i++) {
            PointD pointD = path.get(i);
            data.writeFloat((float) pointD.x);
            data.writeFloat((float) pointD.y);
        }

        Vector2 velocity = damageable.getBody().getLinearVelocity();
        data.writeFloat((float) velocity.x);
        data.writeFloat((float) velocity.y);
        data.writeFloat((float) damageable.getBody().getAngularVelocity());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        posX = data.readFloat();
        posY = data.readFloat();
        sin = data.readFloat();
        cos = data.readFloat();
        sizeX = data.readFloat();
        sizeY = data.readFloat();
        dataIndex = data.readShort();
        x = data.readShort();
        y = data.readShort();
        int maxX = data.readShort();
        int maxY = data.readShort();
        width = maxX - x + 1;
        height = maxY - y + 1;
        int size = width * height;
        byteBuffer = Engine.renderer.createByteBuffer(size);
        data.readBytes(byteBuffer);
        byteBuffer.position(0);

        short paths = data.readShort();
        pathD = new PathD(paths);
        for (int j = 0; j < paths; j++) {
            pathD.add(new PointD(data.readFloat(), data.readFloat()));
        }

        velocityX = data.readFloat();
        velocityY = data.readFloat();
        angularVelocity = data.readFloat();

        wreck = DamageUtils.createDamage(posX, posY, sin, cos, sizeX, sizeY, pathD, null, dataIndex);
    }
}