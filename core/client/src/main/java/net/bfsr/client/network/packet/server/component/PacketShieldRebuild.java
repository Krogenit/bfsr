package net.bfsr.client.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.component.shield.Shield;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;

import java.io.IOException;

public class PacketShieldRebuild implements PacketIn {
    private int id;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            Shield shield = ship.getShield();
            if (shield != null) shield.rebuildShield();
        }
    }
}