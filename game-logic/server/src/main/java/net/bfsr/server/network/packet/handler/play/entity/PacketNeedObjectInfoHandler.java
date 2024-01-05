package net.bfsr.server.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketNeedObjectInfoHandler extends PacketHandler<PacketNeedObjectInfo, PlayerNetworkHandler> {
    @Override
    public void handle(PacketNeedObjectInfo packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(packet.getObjectId());
        if (obj instanceof RigidBody<?> rigidBody && !rigidBody.isDead()) {
            playerNetworkHandler.sendTCPPacket(new PacketSpawnEntity(rigidBody.createSpawnData()));
        }
    }
}