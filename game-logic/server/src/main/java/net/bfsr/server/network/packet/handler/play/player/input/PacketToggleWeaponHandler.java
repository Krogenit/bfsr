package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.GameplayMode;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.client.input.PacketToggleWeapon;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.PlayerInputController;

import java.net.InetSocketAddress;

public class PacketToggleWeaponHandler extends PacketHandler<PacketToggleWeapon, PlayerNetworkHandler> {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();

    @Override
    public void handle(PacketToggleWeapon packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        if (gameLogic.getGameplayMode() != GameplayMode.MMO) {
            return;
        }

        PlayerInputController playerInputController = playerNetworkHandler.getPlayer().getPlayerInputController();
        playerInputController.toggleLeftClickInputState();
    }
}
