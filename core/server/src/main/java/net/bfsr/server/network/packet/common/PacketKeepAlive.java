package net.bfsr.server.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketKeepAlive implements PacketIn, PacketOut {
    private int time;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.time = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(this.time);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        if (time == networkManager.getCurrentTimeInt()) {
            int var2 = (int) (System.nanoTime() / 1000000L - networkManager.getCurrentTime());
            networkManager.getPlayer().setPing((networkManager.getPlayer().getPing() * 3 + var2) / 4);
        }
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}