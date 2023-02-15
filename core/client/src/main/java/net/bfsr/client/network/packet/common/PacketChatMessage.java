package net.bfsr.client.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketChatMessage implements PacketOut, PacketIn {
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
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Core.get().getGuiInGame().addChatMessage(message);
    }
}