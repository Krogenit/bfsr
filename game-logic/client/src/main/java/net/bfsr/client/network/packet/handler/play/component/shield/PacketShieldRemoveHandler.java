package net.bfsr.client.network.packet.handler.play.component.shield;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.ShieldEffects;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketShieldRemove;
import org.joml.Vector4f;

import java.net.InetSocketAddress;

public class PacketShieldRemoveHandler extends PacketHandler<PacketShieldRemove, NetworkSystem> {
    @Override
    public void handle(PacketShieldRemove packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = Client.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Shield shield = ship.getModules().getShield();
            if (shield != null) {
                shield.removeShield();
                Vector4f shipEffectColor = ship.getShipData().getEffectsColor();
                ShieldEffects.disable(ship.getX(), ship.getY(), ship.getSizeX() * 2.0f, shipEffectColor.x, shipEffectColor.y,
                        shipEffectColor.z, 1.0f);
            }
        }
    }
}