package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketLoginSuccess implements PacketOut {
    private String playerName;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeStringToBuffer(this.playerName);
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}