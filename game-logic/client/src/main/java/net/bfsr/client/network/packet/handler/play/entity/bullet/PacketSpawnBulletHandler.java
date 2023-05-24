package net.bfsr.client.network.packet.handler.play.entity.bullet;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.world.WorldClient;
import net.bfsr.config.entity.bullet.BulletData;
import net.bfsr.config.entity.bullet.BulletRegistry;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.bullet.PacketSpawnBullet;
import org.joml.Vector2f;

import java.net.InetSocketAddress;

public class PacketSpawnBulletHandler extends PacketHandler<PacketSpawnBullet, NetworkSystem> {
    @Override
    public void handle(PacketSpawnBullet packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        WorldClient world = Core.get().getWorld();
        GameObject obj = world.getEntityById(packet.getShipId());
        if (obj instanceof Ship ship) {
            BulletData bulletData = BulletRegistry.INSTANCE.get(packet.getDataIndex());
            Vector2f pos = packet.getPos();
            Bullet bullet = new Bullet(pos.x, pos.y, packet.getSin(), packet.getCos(), ship, bulletData);
            bullet.init(world, packet.getId());
            world.addBullet(bullet);
        }
    }
}