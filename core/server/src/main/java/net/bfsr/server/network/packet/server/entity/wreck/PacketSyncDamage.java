package net.bfsr.server.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.damage.Damagable;
import net.bfsr.server.damage.DamageMask;

import java.io.IOException;

@NoArgsConstructor
public class PacketSyncDamage implements PacketOut {
    private Damagable damagable;
    private int x, y, maxX, maxY;
    private PathsD contours;
    private byte[] bytes;

    public PacketSyncDamage(Damagable damagable) {
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
}