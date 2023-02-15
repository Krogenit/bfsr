package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.network.status.ServerStatusResponse;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketServerInfo implements PacketOut {
    private ServerStatusResponse serverStatus;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(serverStatus.getPlayerCountData());
        data.writeStringToBuffer(serverStatus.getVersion());
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}