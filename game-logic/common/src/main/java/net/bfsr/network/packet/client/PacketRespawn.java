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
public class PacketRespawn extends PacketAdapter {
    private float camPosX, camPosY;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeFloat(camPosX);
        data.writeFloat(camPosY);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        camPosX = data.readFloat();
        camPosY = data.readFloat();
    }
}