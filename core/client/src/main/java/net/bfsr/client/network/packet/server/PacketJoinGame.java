package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketJoinGame implements PacketIn {
    private long seed;

    @Override
    public void read(ByteBuf data) throws IOException {
        this.seed = data.readLong();
    }

    @Override
    public void processOnClientSide() {
        Core core = Core.get();
        core.setCurrentGui(null);
        core.getWorld().setSeed(seed);
    }
}