package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.network.packet.common.PacketObjectPosition;

import java.net.InetSocketAddress;

public class PacketObjectPositionHandler extends PacketHandler<PacketObjectPosition, NetworkSystem> {
    @Override
    public void handle(PacketObjectPosition packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        Core core = Core.get();
        RigidBody obj = core.getWorld().getEntityById(packet.getId());
        if (obj != null) {
            obj.updateClientPositionFromPacket(packet.getPosition(), packet.getSin(), packet.getCos(), packet.getVelocity(), packet.getAngularVelocity());
        } else {
            core.sendUDPPacket(new PacketNeedObjectInfo(packet.getId()));
        }
    }
}