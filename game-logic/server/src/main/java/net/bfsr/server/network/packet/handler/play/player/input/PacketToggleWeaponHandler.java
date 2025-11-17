package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.client.input.PacketToggleWeapon;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.PlayerInputController;

import java.net.InetSocketAddress;

public class PacketToggleWeaponHandler extends PacketHandler<PacketToggleWeapon, PlayerNetworkHandler> {
    @Override
    public void handle(PacketToggleWeapon packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        PlayerInputController playerInputController = playerNetworkHandler.getPlayer().getPlayerInputController();
        playerInputController.toggleLeftClickInputState();
    }
}
