package net.bfsr.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.SHIP_MOVE)
public class PacketShipMove extends PacketAdapter {
    private Direction direction;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeByte(direction.ordinal());
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        direction = Direction.values()[data.readByte()];
    }
}