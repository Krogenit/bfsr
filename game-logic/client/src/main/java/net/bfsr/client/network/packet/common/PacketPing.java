package net.bfsr.client.network.packet.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.AsyncPacketIn;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPing implements AsyncPacketIn, PacketOut {
    private long time;

    @Override
    public void read(ByteBuf data) throws IOException {
        this.time = data.readLong();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(System.nanoTime() - Core.get().getNetworkSystem().getHandshakeTime());
    }

    @Override
    public void processOnClientSide(ChannelHandlerContext ctx) {
        Core.get().getGuiManager().getGuiInGame().setPing(time / 1_000_000.0f);
    }
}