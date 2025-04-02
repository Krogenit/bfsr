package net.bfsr.client.network.packet.handler.play.component;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.network.packet.server.component.PacketModuleAdd;

import java.net.InetSocketAddress;

public class PacketAddModuleHandler extends PacketHandler<PacketModuleAdd, NetworkHandler> {
    private final ShipOutfitter shipOutfitter = Client.get().getShipOutfitter();

    @Override
    public void handle(PacketModuleAdd packet, NetworkHandler networkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        int entityId = packet.getEntityId();
        RigidBody entity = Client.get().getWorld().getEntityById(entityId);
        if (entity instanceof Ship ship) {
            if (packet.getType() == ModuleType.SHIELD) {
                shipOutfitter.addShieldToShip(ship, packet.getDataId());
            }
        }
    }
}