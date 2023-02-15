package net.bfsr.server.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketChatMessage implements PacketIn, PacketOut {
    private String message;

    @Override
    public void read(PacketBuffer data) throws IOException {
        message = data.readStringFromBuffer(2048);
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeStringToBuffer(message);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        MainServer.getInstance().getNetworkSystem().sendPacketToAll(new PacketChatMessage(message));
    }
}