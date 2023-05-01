package net.bfsr.client.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.math.Direction;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipMove implements PacketOut {
    private Direction direction;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeByte(direction.ordinal());
    }
}