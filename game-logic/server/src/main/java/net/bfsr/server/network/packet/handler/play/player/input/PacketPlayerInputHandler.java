package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.client.input.PacketPlayerInput;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerInputController;

import java.net.InetSocketAddress;

public class PacketPlayerInputHandler extends PacketHandler<PacketPlayerInput, PlayerNetworkHandler> {
    @Override
    public void handle(PacketPlayerInput packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        playerNetworkHandler.setRenderDelayInFrames(packet.getRenderDelayInFrames());

        Player player = playerNetworkHandler.getPlayer();
        if (player == null) {
            return;
        }

        player.setPosition(packet.getCameraX(), packet.getCameraY());

        PlayerInputController playerInputController = player.getPlayerInputController();
        playerInputController.setMousePosition(packet.getMouseWorldX(), packet.getMouseWorldY());

        boolean[] mouseStates = packet.getMouseStates();
        boolean[] buttonsStates = packet.getButtonsStates();
        playerInputController.setInputStates(mouseStates, buttonsStates);
    }
}
