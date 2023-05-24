package net.bfsr.network.packet.input;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.math.Direction;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketShipStopMove extends PacketAdapter {
    private Direction direction;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeByte(direction.ordinal());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        direction = Direction.values()[data.readByte()];
    }
}