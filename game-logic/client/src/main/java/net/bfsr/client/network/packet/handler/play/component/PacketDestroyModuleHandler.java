package net.bfsr.client.network.packet.handler.play.component;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.component.PacketModuleRemove;

import java.net.InetSocketAddress;

public class PacketDestroyModuleHandler extends PacketHandler<PacketModuleRemove, NetworkHandler> {
    @Override
    public void handle(PacketModuleRemove packet, NetworkHandler networkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        int entityId = packet.getEntityId();
        RigidBody entity = Client.get().getWorld().getEntityById(entityId);
        if (entity instanceof Ship ship) {
            ship.getModules().destroyModule(packet.getId(), packet.getType());
        }
    }
}