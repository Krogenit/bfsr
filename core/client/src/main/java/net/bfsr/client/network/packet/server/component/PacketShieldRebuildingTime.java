package net.bfsr.client.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.GameObject;

import java.io.IOException;

public class PacketShieldRebuildingTime implements PacketIn {
    private int id;
    private int time;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        time = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ShieldCommon shield = ship.getShield();
            if (shield != null) shield.setRebuildingTime(time);
        }
    }
}