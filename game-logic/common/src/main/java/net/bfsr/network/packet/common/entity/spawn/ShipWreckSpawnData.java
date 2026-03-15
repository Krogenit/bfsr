package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.entity.wreck.ShipWreck;
import org.jbox2d.common.Vector2;

@Getter
public class ShipWreckSpawnData extends DamageableRigidBodySpawnData<ShipWreck> {
    private float velocityX, velocityY;
    private float angularVelocity;
    private float localOffsetX, localOffsetY;
    private int shipId;

    @Override
    public void setData(ShipWreck wreck) {
        super.setData(wreck);
        Vector2 linearVelocity = wreck.getLinearVelocity();
        this.velocityX = linearVelocity.x;
        this.velocityY = linearVelocity.y;
        this.angularVelocity = wreck.getAngularVelocity();
        this.localOffsetX = wreck.getLocalOffsetX();
        this.localOffsetY = wreck.getLocalOffsetY();
        this.shipId = wreck.getShipId();
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeFloat(localOffsetX);
        data.writeFloat(localOffsetY);

        super.writeData(data);

        data.writeFloat(velocityX);
        data.writeFloat(velocityY);
        data.writeFloat(angularVelocity);
        data.writeInt(shipId);
    }

    @Override
    public void readData(ByteBuf data) {
        localOffsetX = data.readFloat();
        localOffsetY = data.readFloat();

        super.readData(data);

        velocityX = data.readFloat();
        velocityY = data.readFloat();
        angularVelocity = data.readFloat();
        shipId = data.readInt();
    }
}