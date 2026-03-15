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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.SHIP_JUMP)
public class PacketShipJump extends PacketAdapter {
    private float x;
    private float y;

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
