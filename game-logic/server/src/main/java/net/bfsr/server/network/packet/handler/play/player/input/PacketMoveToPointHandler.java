package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.GameplayMode;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.input.PacketMoveToPoint;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;
import org.joml.Vector2f;

import java.net.InetSocketAddress;

public class PacketMoveToPointHandler extends PacketHandler<PacketMoveToPoint, PlayerNetworkHandler> {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();
    private final AiFactory aiFactory = ServerGameLogic.get().getAiFactory();

    @Override
    public void handle(PacketMoveToPoint packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        if (gameLogic.getGameplayMode() != GameplayMode.MMO) {
            return;
        }

        Player player = playerNetworkHandler.getPlayer();
        Ship ship = player.getShip();
        Vector2f point = packet.getPoint();
        ship.setAi(aiFactory.createFlyToPointAi(point.x, point.y));
    }
}
