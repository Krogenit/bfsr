package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.entity.bullet.Bullet;

@Getter
@NoArgsConstructor
public class BulletSpawnData extends RigidBodySpawnData<Bullet> {
    private int ownerId;
    private int clientId;

    @Override
    public void setData(Bullet bullet) {
        super.setData(bullet);
        this.ownerId = bullet.getOwner().getId();
        this.clientId = bullet.getClientId();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(ownerId);
        data.writeInt(clientId);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        ownerId = data.readInt();
        clientId = data.readInt();
    }

    @Override
    public int getTypeId() {
        return EntityPacketSpawnType.BULLET.ordinal();
    }
}