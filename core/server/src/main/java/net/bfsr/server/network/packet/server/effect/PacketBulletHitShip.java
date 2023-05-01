package net.bfsr.server.network.packet.server.effect;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
public class PacketBulletHitShip implements PacketOut {
    private Bullet bullet;
    private Ship ship;
    private float contactX, contactY;
    private float normalX, normalY;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(bullet.getId());
        data.writeInt(ship.getId());
        data.writeFloat(contactX);
        data.writeFloat(contactY);
        data.writeFloat(normalX);
        data.writeFloat(normalY);
    }
}