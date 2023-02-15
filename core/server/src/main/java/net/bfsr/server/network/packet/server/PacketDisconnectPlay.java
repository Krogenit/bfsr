package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDisconnectPlay implements PacketOut {
    private String message;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeStringToBuffer(this.message);
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}