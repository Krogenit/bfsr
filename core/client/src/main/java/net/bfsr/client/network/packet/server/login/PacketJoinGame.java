package net.bfsr.client.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.ConnectionState;

import java.io.IOException;

public class PacketJoinGame implements PacketIn, PacketOut {
    private long seed;

    @Override
    public void write(ByteBuf data) throws IOException {

    }

    @Override
    public void read(ByteBuf data) throws IOException {
        this.seed = data.readLong();
    }

    @Override
    public void processOnClientSide() {
        Core core = Core.get();
        WorldClient world = core.createWorld();
        world.setSeed(seed);
        core.setCurrentGui(null);
        core.getNetworkSystem().setConnectionState(ConnectionState.PLAY);
    }
}