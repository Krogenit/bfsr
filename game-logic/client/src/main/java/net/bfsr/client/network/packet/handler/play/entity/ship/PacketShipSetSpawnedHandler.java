package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.entity.ship.PacketShipSetSpawned;

import java.net.InetSocketAddress;

public class PacketShipSetSpawnedHandler extends PacketHandler<PacketShipSetSpawned, NetworkSystem> {
    @Override
    public void handle(PacketShipSetSpawned packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject gameObject = Client.get().getWorld().getEntityById(packet.getId());
        if (gameObject instanceof Ship ship) {
            ship.setSpawned();
        }
    }
}