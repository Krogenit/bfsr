package net.bfsr.server.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;
import org.joml.Vector2f;

import java.io.IOException;

public class PacketSyncPlayerMousePosition implements PacketIn {
    private final Vector2f mousePosition = new Vector2f();

    @Override
    public void read(ByteBuf data) throws IOException {
        ByteBufUtils.readVector(data, mousePosition);
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.getPlayer().getPlayerInputController().setMousePosition(mousePosition);
    }
}