package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;

import java.io.IOException;

public class PacketRemoveObject implements PacketIn {
    private int id;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj != null) obj.setDead();
    }
}