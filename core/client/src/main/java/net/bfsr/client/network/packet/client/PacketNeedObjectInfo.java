package net.bfsr.client.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

@AllArgsConstructor
@NoArgsConstructor
public class PacketNeedObjectInfo implements PacketOut {
    private int objectId;

    @Override
    public void write(ByteBuf data) {
        data.writeInt(objectId);
    }
}