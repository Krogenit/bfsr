package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

@AllArgsConstructor
@NoArgsConstructor
public class PacketCameraPosition implements PacketIn {
    private float x, y;

    @Override
    public void read(ByteBuf data) {
        x = data.readFloat();
        y = data.readFloat();
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.getPlayer().setPosition(x, y);
    }
}