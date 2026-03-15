package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.GameplayMode;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.client.input.PacketPlayerInput;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerInputController;

import java.net.InetSocketAddress;

public class PacketPlayerInputHandler extends PacketHandler<PacketPlayerInput, PlayerNetworkHandler> {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();

    @Override
    public void handle(PacketPlayerInput packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        if (gameLogic.getGameplayMode() != GameplayMode.SESSION) {
            return;
        }

        playerNetworkHandler.setRenderDelayInFrames(packet.getRenderDelayInFrames());

        Player player = playerNetworkHandler.getPlayer();
        if (player == null) {
            return;
        }

        PlayerInputController playerInputController = player.getPlayerInputController();
        playerInputController.setMousePosition(packet.getMouseWorldX(), packet.getMouseWorldY());

        boolean[] mouseStates = packet.getMouseStates();
        boolean[] buttonsStates = packet.getButtonsStates();
        playerInputController.setInputStates(mouseStates, buttonsStates);
    }
}
