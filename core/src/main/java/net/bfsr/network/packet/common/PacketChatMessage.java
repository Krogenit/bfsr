package net.bfsr.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketChatMessage extends Packet {

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

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        server.getNetworkSystem().sendPacketToAll(new PacketChatMessage(message));
    }
}