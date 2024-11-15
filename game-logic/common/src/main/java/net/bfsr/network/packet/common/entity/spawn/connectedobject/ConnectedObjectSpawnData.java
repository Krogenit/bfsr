package net.bfsr.network.packet.common.entity.spawn.connectedobject;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.damage.ConnectedObject;

@Getter
public abstract class ConnectedObjectSpawnData {
    private int configConvertedId;
    private int configId;

    public void readData(ByteBuf data) {
        configConvertedId = data.readInt();
        configId = data.readInt();
    }

    public abstract ConnectedObject<?> create(ConfigData configData);
}
