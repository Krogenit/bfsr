package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.ship.PacketSyncMoveDirection;

import java.net.InetSocketAddress;

public class PacketSyncMoveDirectionHandler extends PacketHandler<PacketSyncMoveDirection, NetworkSystem> {
    private final Direction[] directions = Direction.values();

    @Override
    public void handle(PacketSyncMoveDirection packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Direction direction = directions[packet.getDirection()];

            if (packet.isRemove()) {
                ship.removeMoveDirection(direction);
            } else {
                ship.addMoveDirection(direction);
            }
        }
    }
}