package net.bfsr.client.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
public class PacketLoginTCP implements PacketOut {
    private String login;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, login);
    }
}
