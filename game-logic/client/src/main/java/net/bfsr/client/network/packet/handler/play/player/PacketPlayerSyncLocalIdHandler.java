package net.bfsr.client.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.world.entity.ClientEntityIdManager;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.server.player.PacketPlayerSyncLocalId;

import java.net.InetSocketAddress;

public class PacketPlayerSyncLocalIdHandler extends PacketHandler<PacketPlayerSyncLocalId, NetworkSystem> {
    private final ClientEntityIdManager entityIdManager = Client.get().getEntityIdManager();

    @Override
    public void handle(PacketPlayerSyncLocalId packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        entityIdManager.addRemoteData(packet.getLocalId(), packet.getTick());
    }
}
