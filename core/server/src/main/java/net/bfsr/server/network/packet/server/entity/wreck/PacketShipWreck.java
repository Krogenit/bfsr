package net.bfsr.server.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PointD;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.damage.Damagable;
import net.bfsr.server.damage.DamageMask;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipWreck implements PacketOut {
    private Damagable damagable;
    private int x, y, maxX, maxY;
    private PathD path;

    public PacketShipWreck(Damagable damagable) {
        this.damagable = damagable;
        DamageMask damageMask = damagable.getMask();
        x = damageMask.getX();
        y = damageMask.getY();
        maxX = damageMask.getMaxX();
        maxY = damageMask.getMaxY();
        path = damagable.getContours().get(0);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(damagable.getId());
        data.writeFloat(damagable.getX());
        data.writeFloat(damagable.getY());
        data.writeFloat(damagable.getSin());
        data.writeFloat(damagable.getCos());
        Vector2f scale = damagable.getScale();
        data.writeFloat(scale.x);
        data.writeFloat(scale.y);
        data.writeShort(damagable.getTextureIndex());
        DamageMask damageMask = damagable.getMask();
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

        Vector2 velocity = damagable.getBody().getLinearVelocity();
        data.writeFloat((float) velocity.x);
        data.writeFloat((float) velocity.y);
        data.writeFloat((float) damagable.getBody().getAngularVelocity());
    }
}