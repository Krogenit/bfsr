package net.bfsr.client.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketChatMessage implements PacketOut, PacketIn {
    private String message;

    @Override
    public void read(ByteBuf data) throws IOException {
        message = ByteBufUtils.readString(data);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, message);
    }

    @Override
    public void processOnClientSide() {
        Core.get().getGuiInGame().addChatMessage(message);
    }
}