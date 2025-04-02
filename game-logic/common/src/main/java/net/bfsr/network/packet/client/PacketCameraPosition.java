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
@PacketAnnotation(id = PacketIdRegistry.CAMERA_POSITION)
public class PacketCameraPosition extends PacketAdapter {
    private float x, y;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeFloat(x);
        data.writeFloat(y);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) {
        x = data.readFloat();
        y = data.readFloat();
    }
}