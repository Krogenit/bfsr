package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.world.entity.EntitySpawnLogicType;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;

import java.net.InetSocketAddress;

public class PacketSpawnEntityHandler extends PacketHandler<PacketSpawnEntity, NetworkSystem> {
    private static final EntitySpawnLogicType[] SPAWN_LOGIC_TYPES = EntitySpawnLogicType.values();

    @Override
    public void handle(PacketSpawnEntity packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        if (Core.get().getWorld().getEntityById(packet.getEntityPacketSpawnData().getEntityId()) == null) {
            EntitySpawnLogicType spawnLogicType = SPAWN_LOGIC_TYPES[packet.getEntityPacketSpawnData().getType().ordinal()];
            spawnLogicType.spawn(packet.getEntityPacketSpawnData());
        }
    }
}