package net.bfsr.server.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PointD;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.Damageable;
import net.bfsr.network.PacketOut;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipWreck implements PacketOut {
    private Damageable damageable;
    private int x, y, maxX, maxY;
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
}