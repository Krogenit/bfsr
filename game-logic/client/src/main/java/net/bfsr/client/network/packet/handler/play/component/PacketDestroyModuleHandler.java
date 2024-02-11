package net.bfsr.client.network.packet.handler.play.component;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.NetworkHandler;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketDestroyModule;

import java.net.InetSocketAddress;

public class PacketDestroyModuleHandler extends PacketHandler<PacketDestroyModule, NetworkHandler> {
    @Override
    public void handle(PacketDestroyModule packet, NetworkHandler networkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        int entityId = packet.getEntityId();
        RigidBody<?> entity = Core.get().getWorld().getEntityById(entityId);
        if (entity instanceof Ship ship) {
            ship.getModules().destroyModule(packet.getId(), packet.getType());
        } else {
            System.out.println("CAN'T DESTROY MODULE ENTITY NOT FOUND");
        }
    }
}