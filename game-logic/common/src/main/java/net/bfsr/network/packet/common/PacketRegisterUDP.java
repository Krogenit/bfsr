package net.bfsr.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacketRegisterUDP extends PacketAdapter {
    private int connectionId;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(connectionId);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        connectionId = data.readInt();
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}