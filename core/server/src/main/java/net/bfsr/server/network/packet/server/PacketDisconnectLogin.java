package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDisconnectLogin implements PacketOut {
    private String message;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, message);
    }
}