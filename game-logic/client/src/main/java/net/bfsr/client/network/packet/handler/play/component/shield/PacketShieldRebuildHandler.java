package net.bfsr.client.network.packet.handler.play.component.shield;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.ShieldEffects;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.component.PacketShieldRebuild;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.net.InetSocketAddress;

public class PacketShieldRebuildHandler extends PacketHandler<PacketShieldRebuild, NetworkSystem> {
    @Override
    public void handle(PacketShieldRebuild packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = Core.get().getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Shield shield = ship.getModules().getShield();
            if (shield != null) {
                shield.rebuildShield();

                Vector2f position = ship.getPosition();
                Vector4f shipEffectColor = ship.getConfigData().getEffectsColor();
                ShieldEffects.rebuild(position.x, position.y, ship.getSize().x * 2.0f, shipEffectColor.x, shipEffectColor.y,
                        shipEffectColor.z, 1.0f);
            }
        }
    }
}