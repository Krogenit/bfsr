package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.world.entity.EntitySpawnLogicType;
import net.bfsr.client.world.entity.EntitySpawnLoginRegistry;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;
import net.bfsr.world.World;

import java.net.InetSocketAddress;

public class PacketSpawnEntityHandler extends PacketHandler<PacketSpawnEntity, NetworkSystem> {
    private static final EntitySpawnLogicType[] SPAWN_LOGIC_TYPES = EntitySpawnLogicType.values();

    private final EntitySpawnLoginRegistry entitySpawnLoginRegistry = Client.get().getEntitySpawnLoginRegistry();

    @Override
    public void handle(PacketSpawnEntity packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        World world = Client.get().getWorld();
        if (world.getEntityById(packet.getEntityPacketSpawnData().getEntityId()) == null) {
            entitySpawnLoginRegistry.spawn(SPAWN_LOGIC_TYPES[packet.getEntityPacketSpawnData().getType().ordinal()],
                    packet.getEntityPacketSpawnData(), world);
        }
    }
}