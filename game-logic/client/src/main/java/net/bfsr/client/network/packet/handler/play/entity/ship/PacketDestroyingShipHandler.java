package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.entity.ship.PacketShipSetDestroying;

import java.net.InetSocketAddress;

public class PacketDestroyingShipHandler extends PacketHandler<PacketShipSetDestroying, NetworkSystem> {
    @Override
    public void handle(PacketShipSetDestroying packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = Client.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            ship.setDestroying();
        }
    }
}