package net.bfsr.client.network.packet.handler.play.component.hull;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketHullInfo;

import java.net.InetSocketAddress;

public class PacketHullInfoHandler extends PacketHandler<PacketHullInfo, NetworkSystem> {
    @Override
    public void handle(PacketHullInfo packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            ship.getHull().setHull(packet.getHull());
        }
    }
}