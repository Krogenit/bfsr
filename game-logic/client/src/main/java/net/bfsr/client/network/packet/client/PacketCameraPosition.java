package net.bfsr.client.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketCameraPosition implements PacketOut {
    private float x, y;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeFloat(x);
        data.writeFloat(y);
    }
}