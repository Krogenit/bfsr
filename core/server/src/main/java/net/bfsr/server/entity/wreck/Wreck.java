package net.bfsr.server.entity.wreck;

import net.bfsr.entity.wreck.WreckCommon;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.server.PacketRemoveObject;
import net.bfsr.server.world.WorldServer;

public class Wreck extends WreckCommon {
    public Wreck init(WorldServer world, int id, int wreckIndex, boolean light, boolean fire, boolean fireExplosion, float x, float y, float velocityX, float velocityY, float rotation,
                      float angularVelocity, float scaleX, float scaleY, float r, float g, float b, float a, float alphaVelocity, WreckType wreckType) {
        init(world, id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, wreckIndex, fire, light,
                fireExplosion, 10, 0, wreckType, WreckRegistry.INSTANCE.getWreck(wreckType, wreckIndex));
        return this;
    }

    @Override
    protected void addParticle() {
        ((WorldServer) world).addWreck(this);
    }

    @Override
    public void update() {
        super.update();
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketObjectPosition(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    protected void onLifeTimeEnded() {
        destroy();
    }

    @Override
    protected void onHullDamage() {
        if (hull <= 0) {
            destroy();
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }
}
