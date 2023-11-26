package net.bfsr.network.packet.common.entity.spawn;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.bullet.Bullet;

@Getter
@NoArgsConstructor
public class BulletSpawnData extends RigidBodySpawnData {
    public BulletSpawnData(Bullet bullet) {
        super(bullet);
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.BULLET;
    }
}