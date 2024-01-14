package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.wreck.ShipWreck;
import org.joml.Vector2f;

@NoArgsConstructor
@Getter
public class ShipWreckSpawnData extends DamageableRigidBodySpawnData<ShipWreck> {
    private float velocityX, velocityY;
    private float angularVelocity;

    public ShipWreckSpawnData(ShipWreck wreck) {
        super(wreck);
        Vector2f velocity = wreck.getVelocity();
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.angularVelocity = wreck.getAngularVelocity();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);

        data.writeFloat(velocityX);
        data.writeFloat(velocityY);
        data.writeFloat(angularVelocity);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        velocityX = data.readFloat();
        velocityY = data.readFloat();
        angularVelocity = data.readFloat();
    }

    @Override
    protected ShipWreck createRigidBody() {
        ShipData shipData = ShipRegistry.INSTANCE.get(dataId);
        return new ShipWreck(posX, posY, sin, cos, shipData.getSizeX(), shipData.getSizeY(), shipData,
                new DamageMask(maskWidth, maskHeight, null), contours);
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP_WRECK;
    }
}