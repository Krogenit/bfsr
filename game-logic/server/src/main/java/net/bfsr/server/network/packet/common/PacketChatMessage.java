package net.bfsr.server.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.core.Server;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketChatMessage implements PacketIn, PacketOut {
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
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        Server.getInstance().getNetworkSystem().sendTCPPacketToAll(new PacketChatMessage(message));
    }
}