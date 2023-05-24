package net.bfsr.client.network.packet.handler.play.entity.wreck;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.wreck.PacketShipWreck;
import net.bfsr.world.World;

import java.net.InetSocketAddress;

public class PacketShipWreckHandler extends PacketHandler<PacketShipWreck, NetworkSystem> {
    @Override
    public void handle(PacketShipWreck packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        ShipWreck wreck = packet.getWreck();
        if (wreck != null) {
            World world = Core.get().getWorld();
            wreck.init(world, packet.getId());
            world.addWreck(wreck);
            DamageHandler.updateDamage(wreck, packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight(), packet.getByteBuffer());
            wreck.getBody().setLinearVelocity(packet.getVelocityX(), packet.getVelocityY());
            wreck.getBody().setAngularVelocity(packet.getAngularVelocity());
        }
    }
}