package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.damage.Damageable;
import net.bfsr.entity.GameObject;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.wreck.PacketSyncDamage;
import org.dyn4j.dynamics.BodyFixture;

import java.net.InetSocketAddress;
import java.util.List;

public class PacketSyncDamageHandler extends PacketHandler<PacketSyncDamage, NetworkSystem> {
    private final Core core = Core.get();
    private final DamageHandler damageHandler = core.getDamageHandler();

    @Override
    public void handle(PacketSyncDamage packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject gameObject = core.getWorld().getEntityById(packet.getId());
        if (gameObject instanceof Damageable damageable) {
            damageable.setContours(packet.getContours());
            List<BodyFixture> fixtures = packet.getFixtures();
            if (fixtures.size() > 0) {
                damageable.setFixtures(fixtures);
            }

            damageHandler.updateDamage(damageable, packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight(),
                    packet.getByteBuffer());
        }
    }
}