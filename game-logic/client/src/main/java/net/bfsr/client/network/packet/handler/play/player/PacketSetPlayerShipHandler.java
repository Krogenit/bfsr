package net.bfsr.client.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.player.PacketSetPlayerShip;
import net.bfsr.world.World;

import java.net.InetSocketAddress;

public class PacketSetPlayerShipHandler extends PacketHandler<PacketSetPlayerShip, NetworkSystem> {
    @Override
    public void handle(PacketSetPlayerShip packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        Core core = Core.get();
        World world = core.getWorld();
        GameObject obj = world.getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            core.getInputHandler().getPlayerInputController().setShip(ship);
        }
    }
}