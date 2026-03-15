package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.PacketShipJump;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerInputController;

import java.net.InetSocketAddress;

@Log4j2
public class PacketShipJumpHandler extends PacketHandler<PacketShipJump, PlayerNetworkHandler> {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();
    private final AiFactory aiFactory = gameLogic.getAiFactory();

    @Override
    public void handle(PacketShipJump packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        Player player = playerNetworkHandler.getPlayer();
        PlayerInputController playerInputController = player.getPlayerInputController();
        Ship ship = playerInputController.getShip();
        if (ship == null) {
            log.error("Player {} trying to jump without ship", player.getUsername());
            return;
        }

        ship.setAi(aiFactory.createJumpAi(packet.getX(), packet.getY()));
    }
}
