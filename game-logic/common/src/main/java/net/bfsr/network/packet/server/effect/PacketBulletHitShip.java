package net.bfsr.network.packet.server.effect;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketBulletHitShip extends PacketAdapter {
    private Bullet bullet;
    private Ship ship;
    private int bulletId, shipId;
    private float contactX, contactY;
    private float normalX, normalY;

    public PacketBulletHitShip(Bullet bullet, Ship ship, float contactX, float contactY, float normalX, float normalY) {
        this.bullet = bullet;
        this.ship = ship;
        this.contactX = contactX;
        this.contactY = contactY;
        this.normalX = normalX;
        this.normalY = normalY;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(bullet.getId());
        data.writeInt(ship.getId());
        data.writeFloat(contactX);
        data.writeFloat(contactY);
        data.writeFloat(normalX);
        data.writeFloat(normalY);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        bulletId = data.readInt();
        shipId = data.readInt();
        contactX = data.readFloat();
        contactY = data.readFloat();
        normalX = data.readFloat();
        normalY = data.readFloat();
    }
}