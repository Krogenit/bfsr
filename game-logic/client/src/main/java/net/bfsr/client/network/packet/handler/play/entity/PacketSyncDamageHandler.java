package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.PacketSyncDamage;

import java.net.InetSocketAddress;

public class PacketSyncDamageHandler extends PacketHandler<PacketSyncDamage, NetworkSystem> {
    private final Core core = Core.get();
    private final DamageHandler damageHandler = core.getDamageHandler();

    @Override
    public void handle(PacketSyncDamage packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        RigidBody<?> rigidBody = core.getWorld().getEntityById(packet.getId());
        if (rigidBody instanceof DamageableRigidBody<?> damageableRigidBody) {
            damageableRigidBody.setContours(packet.getContours());
            damageableRigidBody.setFixtures(packet.getFixtures());
            damageHandler.updateDamage(damageableRigidBody, packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight(),
                    packet.getByteBuffer());
        }
    }
}