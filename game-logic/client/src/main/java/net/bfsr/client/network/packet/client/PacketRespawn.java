package net.bfsr.client.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRespawn implements PacketOut {
    private float camPosX, camPosY;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeFloat(camPosX);
        data.writeFloat(camPosY);
    }
}