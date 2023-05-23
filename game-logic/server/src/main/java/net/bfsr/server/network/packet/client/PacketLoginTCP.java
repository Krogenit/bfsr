package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

@Log4j2
public class PacketLoginTCP implements PacketIn {
    private String login;

    @Override
    public void read(ByteBuf data) throws IOException {
        login = ByteBufUtils.readString(data);
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.loginTCP(login);
    }
}
