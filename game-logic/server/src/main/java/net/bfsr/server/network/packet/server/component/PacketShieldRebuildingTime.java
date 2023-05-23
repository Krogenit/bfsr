package net.bfsr.server.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShieldRebuildingTime implements PacketOut {
    private int id;
    private int time;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeInt(time);
    }
}