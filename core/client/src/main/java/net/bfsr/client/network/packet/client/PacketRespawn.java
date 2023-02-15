package net.bfsr.client.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRespawn implements PacketOut {
    private float camPosX, camPosY;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeFloat(camPosX);
        data.writeFloat(camPosY);
    }
}