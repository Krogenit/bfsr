package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.ship.PacketShipSetSpawned;

import java.net.InetSocketAddress;

public class PacketShipSetSpawnedHandler extends PacketHandler<PacketShipSetSpawned, NetworkSystem> {
    @Override
    public void handle(PacketShipSetSpawned packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject gameObject = Core.get().getWorld().getEntityById(packet.getId());
        if (gameObject instanceof Ship ship) {
            ship.setSpawned();
        }
    }
}