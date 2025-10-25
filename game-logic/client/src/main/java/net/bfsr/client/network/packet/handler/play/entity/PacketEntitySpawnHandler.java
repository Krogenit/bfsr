package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.world.entity.EntitySpawnDataRegistry;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.common.world.entity.PacketEntitySpawn;
import net.bfsr.engine.world.World;

import java.net.InetSocketAddress;

public class PacketEntitySpawnHandler extends PacketHandler<PacketEntitySpawn, NetworkSystem> {
    private final EntitySpawnDataRegistry entitySpawnDataRegistry = Client.get().getEntitySpawnDataRegistry();

    @Override
    public void handle(PacketEntitySpawn packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        World world = Client.get().getWorld();
        if (world.getEntityById(packet.getEntityPacketSpawnData().getEntityId()) == null) {
            entitySpawnDataRegistry.spawn(packet.getEntityPacketSpawnData().getTypeId(),
                    packet.getEntityPacketSpawnData(), world);
        }
    }
}