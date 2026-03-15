package net.bfsr.client.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.Engine;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.network.packet.server.player.PacketSetPlayerCameraPosition;

import java.net.InetSocketAddress;

public class PacketSetPlayerCameraPositionHandler extends PacketHandler<PacketSetPlayerCameraPosition, NetworkSystem> {
    private final AbstractCamera camera = Engine.getRenderer().getCamera();

    @Override
    public void handle(PacketSetPlayerCameraPosition packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        camera.setPosition(packet.getX(), packet.getY());
    }
}
