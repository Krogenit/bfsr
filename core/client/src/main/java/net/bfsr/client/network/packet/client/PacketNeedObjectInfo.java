package net.bfsr.client.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

@AllArgsConstructor
@NoArgsConstructor
public class PacketNeedObjectInfo implements PacketOut {
    private int objectId;

    @Override
    public void write(PacketBuffer data) {
        data.writeInt(objectId);
    }
}