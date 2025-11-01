package net.bfsr.network.packet.server.player;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.PLAYER_SET_CAMERA)
public class PacketSetPlayerCameraPosition extends PacketScheduled {
    private float x, y;

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeFloat(x);
        data.writeFloat(y);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        x = data.readFloat();
        y = data.readFloat();
    }
}
