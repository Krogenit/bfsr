package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.entity.Station;

public class StationSpawnData extends RigidBodySpawnData<Station> {
    @Override
    public void setData(Station rigidBody) {
        super.setData(rigidBody);
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
    }
}
