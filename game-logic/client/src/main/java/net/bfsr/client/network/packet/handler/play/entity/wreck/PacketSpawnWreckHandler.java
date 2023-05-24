package net.bfsr.client.network.packet.handler.play.entity.wreck;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.wreck.PacketSpawnWreck;
import net.bfsr.world.World;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class PacketSpawnWreckHandler extends PacketHandler<PacketSpawnWreck, NetworkSystem> {
    private final Supplier<Wreck> wreckSupplier = Wreck::new;

    @Override
    public void handle(PacketSpawnWreck packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        World world = Core.get().getWorld();
        if (world.getEntityById(packet.getId()) == null) {
            world.addWreck(World.WREAK_POOL.getOrCreate(wreckSupplier).init(
                    world, packet.getId(), packet.getWreckIndex(), packet.isLight(), packet.isFire(), packet.isFireExplosion(),
                    packet.getPos().x, packet.getPos().y, packet.getVelocity().x, packet.getVelocity().y, packet.getSin(), packet.getCos(),
                    packet.getRotationSpeed(), packet.getSize().x, packet.getSize().y, packet.getAlphaVelocity(), packet.getWreckType())
            );
        }
    }
}