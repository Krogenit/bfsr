package net.bfsr.network.packet.server.effect;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.damage.DamageType;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketBulletHitShip extends PacketAdapter {
    private static final DamageType[] DAMAGE_TYPES = DamageType.values();

    private Bullet bullet;
    private Ship ship;
    private int bulletId, shipId;
    private float contactX, contactY;
    private float normalX, normalY;
    private DamageType damageType;

    public PacketBulletHitShip(Bullet bullet, Ship ship, float contactX, float contactY, float normalX, float normalY,
                               DamageType damageType) {
        this.bullet = bullet;
        this.ship = ship;
        this.contactX = contactX;
        this.contactY = contactY;
        this.normalX = normalX;
        this.normalY = normalY;
        this.damageType = damageType;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(bullet.getId());
        data.writeInt(ship.getId());
        data.writeFloat(contactX);
        data.writeFloat(contactY);
        data.writeFloat(normalX);
        data.writeFloat(normalY);
        data.writeByte(damageType.ordinal());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        bulletId = data.readInt();
        shipId = data.readInt();
        contactX = data.readFloat();
        contactY = data.readFloat();
        normalX = data.readFloat();
        normalY = data.readFloat();
        damageType = DAMAGE_TYPES[data.readByte()];
    }
}