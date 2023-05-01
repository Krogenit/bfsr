package net.bfsr.client.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PointD;
import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.damage.DamageUtils;
import net.bfsr.entity.wreck.ShipWreck;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PacketShipWreck implements PacketIn {
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
        byteBuffer = BufferUtils.createByteBuffer(size);
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

        wreck = DamageUtils.createDamage(Core.get().getWorld(), id, posX, posY, sin, cos, sizeX, sizeY, pathD, null, dataIndex);
    }

    @Override
    public void processOnClientSide() {
        if (wreck != null) {
            Core.get().getWorld().addWreck(wreck);
            DamageHandler.updateDamage(wreck, x, y, width, height, byteBuffer);
            wreck.getBody().setLinearVelocity(velocityX, velocityY);
            wreck.getBody().setAngularVelocity(angularVelocity);
        }
    }
}