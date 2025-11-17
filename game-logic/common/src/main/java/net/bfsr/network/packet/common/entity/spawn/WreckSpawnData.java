package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.entity.wreck.Wreck;

@Getter
@NoArgsConstructor
public class WreckSpawnData extends RigidBodySpawnData<Wreck> {
    private int maxLifeTime;
    private boolean emitFire;
    private float sizeX, sizeY;
    private int destroyedShipId;

    @Override
    public void setData(Wreck wreck) {
        super.setData(wreck);
        this.emitFire = wreck.isEmitFire();
        this.maxLifeTime = wreck.getMaxLifeTime();
        this.sizeX = wreck.getSizeX();
        this.sizeY = wreck.getSizeY();
        this.destroyedShipId = wreck.getDestroyedShipId();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeBoolean(emitFire);
        data.writeInt(maxLifeTime);

        data.writeFloat(sizeX);
        data.writeFloat(sizeY);
        data.writeInt(destroyedShipId);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        emitFire = data.readBoolean();
        maxLifeTime = data.readInt();

        sizeX = data.readFloat();
        sizeY = data.readFloat();
        destroyedShipId = data.readInt();
    }
}