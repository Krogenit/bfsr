package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.entity.ship.PacketShipSyncMoveDirection;

import java.net.InetSocketAddress;

public class PacketSyncMoveDirectionHandler extends PacketHandler<PacketShipSyncMoveDirection, NetworkSystem> {
    private final Direction[] directions = Direction.values();

    @Override
    public void handle(PacketShipSyncMoveDirection packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = Client.get().getWorld().getEntityById(packet.getId());
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