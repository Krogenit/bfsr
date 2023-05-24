package net.bfsr.server.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketNeedObjectInfo;
import net.bfsr.network.packet.server.entity.bullet.PacketSpawnBullet;
import net.bfsr.network.packet.server.entity.ship.PacketSpawnShip;
import net.bfsr.network.packet.server.entity.wreck.PacketSpawnWreck;
import net.bfsr.network.packet.server.player.PacketSetPlayerShip;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketNeedObjectInfoHandler extends PacketHandler<PacketNeedObjectInfo, PlayerNetworkHandler> {
    @Override
    public void handle(PacketNeedObjectInfo packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(packet.getObjectId());
        if (obj != null) {
            if (obj instanceof Ship ship) {
                playerNetworkHandler.sendTCPPacket(new PacketSpawnShip(ship));

                if (playerNetworkHandler.getPlayer().getPlayerInputController().getShip() == ship) {
                    playerNetworkHandler.sendTCPPacket(new PacketSetPlayerShip(ship.getId()));
                }
            } else if (obj instanceof Bullet bullet) {
                playerNetworkHandler.sendTCPPacket(new PacketSpawnBullet(bullet));
            } else if (obj instanceof Wreck wreck) {
                playerNetworkHandler.sendTCPPacket(new PacketSpawnWreck(wreck));
            }
        }
    }
}