package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.RESPAWN)
public class PacketRespawn extends PacketAdapter {
    private float camPosX, camPosY;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeFloat(camPosX);
        data.writeFloat(camPosY);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        camPosX = data.readFloat();
        camPosY = data.readFloat();
    }
}