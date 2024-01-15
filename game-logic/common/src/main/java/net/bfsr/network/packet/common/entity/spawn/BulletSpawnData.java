package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.bullet.Bullet;

@Getter
@NoArgsConstructor
public class BulletSpawnData extends RigidBodySpawnData {
    private int ownerId;

    public BulletSpawnData(Bullet bullet) {
        super(bullet);
        this.ownerId = bullet.getOwner().getId();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(ownerId);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        ownerId = data.readInt();
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.BULLET;
    }
}