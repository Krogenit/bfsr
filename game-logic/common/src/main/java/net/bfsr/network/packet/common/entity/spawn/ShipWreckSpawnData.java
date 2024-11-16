package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.wreck.ShipWreck;
import org.jbox2d.common.Vector2;

@NoArgsConstructor
@Getter
public class ShipWreckSpawnData extends DamageableRigidBodySpawnData {
    private float velocityX, velocityY;
    private float angularVelocity;
    private float localOffsetX, localOffsetY;

    public ShipWreckSpawnData(ShipWreck wreck) {
        super(wreck);
        Vector2 linearVelocity = wreck.getLinearVelocity();
        this.velocityX = linearVelocity.x;
        this.velocityY = linearVelocity.y;
        this.angularVelocity = wreck.getAngularVelocity();
        this.localOffsetX = wreck.getLocalOffsetX();
        this.localOffsetY = wreck.getLocalOffsetY();
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeFloat(localOffsetX);
        data.writeFloat(localOffsetY);

        super.writeData(data);

        data.writeFloat(velocityX);
        data.writeFloat(velocityY);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void readData(ByteBuf data) {
        localOffsetX = data.readFloat();
        localOffsetY = data.readFloat();

        super.readData(data);

        velocityX = data.readFloat();
        velocityY = data.readFloat();
        angularVelocity = data.readFloat();
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP_WRECK;
    }
}