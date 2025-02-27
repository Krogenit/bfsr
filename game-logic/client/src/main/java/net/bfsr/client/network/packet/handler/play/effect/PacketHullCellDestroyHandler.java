package net.bfsr.client.network.packet.handler.play.effect;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.engine.entity.RigidBody;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.network.packet.server.effect.PacketHullCellDestroy;

import java.net.InetSocketAddress;

public class PacketHullCellDestroyHandler extends PacketHandler<PacketHullCellDestroy, NetworkHandler> {
    private final Client client = Client.get();
    private final ExplosionEffects explosionEffects = client.getParticleEffects().getExplosionEffects();

    @Override
    public void handle(PacketHullCellDestroy packet, NetworkHandler networkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        int entityId = packet.getEntityId();
        World world = client.getWorld();
        RigidBody rigidBody = world.getEntityById(entityId);
        if (rigidBody instanceof Ship ship) {
            HullCell[][] cells = ship.getModules().getHull().getCells();
            HullCell cell = cells[packet.getCellX()][packet.getCellY()];

            int lengthX = cells.length;
            int lengthY = cells[0].length;
            float sizeX = rigidBody.getSizeX();
            float sizeY = rigidBody.getSizeY();
            float halfSizeX = sizeX * 0.5f;
            float halfSizeY = sizeY * 0.5f;
            float rhombusWidth = sizeX / lengthX;
            float rhombusHeight = sizeY / lengthY;
            float halfRhombusWidth = rhombusWidth * 0.5f;
            float halfRhombusHeight = rhombusHeight * 0.5f;
            float posX = cell.getColumn() * rhombusWidth - halfSizeX + halfRhombusWidth;
            float posY = cell.getRow() * rhombusHeight - halfSizeY + halfRhombusHeight;
            float sin = ship.getSin();
            float cos = ship.getCos();
            float rotatedX = posX * cos - posY * sin + rigidBody.getX();
            float rotatedY = posY * cos + posX * sin + rigidBody.getY();

            explosionEffects.spawnSmallExplosion(rotatedX, rotatedY, Math.max(rhombusWidth, rhombusHeight));
        }
    }
}
