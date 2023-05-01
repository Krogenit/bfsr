package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;
import net.bfsr.server.player.PlayerManager;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRespawn implements PacketIn {
    private float camPosX, camPosY;

    @Override
    public void read(ByteBuf data) throws IOException {
        camPosX = data.readFloat();
        camPosY = data.readFloat();
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        PlayerManager.get().respawnPlayer(playerNetworkHandler.getPlayer(), camPosX, camPosY);
    }
}