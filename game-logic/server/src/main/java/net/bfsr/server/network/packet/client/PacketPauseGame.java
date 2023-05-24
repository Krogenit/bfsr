package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.bfsr.engine.Engine;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

public class PacketPauseGame implements PacketIn {
    @Override
    public void read(ByteBuf data) throws IOException {}

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.getServer().setPaused(!Engine.isPaused());
    }
}