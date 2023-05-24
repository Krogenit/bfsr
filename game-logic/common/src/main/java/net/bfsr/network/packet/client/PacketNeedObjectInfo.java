package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketNeedObjectInfo extends PacketAdapter {
    private int objectId;

    @Override
    public void write(ByteBuf data) {
        data.writeInt(objectId);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        objectId = data.readInt();
    }
}