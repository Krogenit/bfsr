package net.bfsr.client.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.AsyncPacketIn;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDisconnectLogin implements AsyncPacketIn {
    private String message;

    @Override
    public void read(ByteBuf data) throws IOException {
        message = ByteBufUtils.readString(data);
    }

    @Override
    public void processOnClientSide(ChannelHandlerContext ctx) {
        Core.get().getNetworkSystem().closeChannels();
        Core.get().getNetworkSystem().onDisconnect(message);
    }
}