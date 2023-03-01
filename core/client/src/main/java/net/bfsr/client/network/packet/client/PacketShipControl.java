package net.bfsr.client.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipControl implements PacketOut {
    private int id;
    private boolean control;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeBoolean(control);
    }
}