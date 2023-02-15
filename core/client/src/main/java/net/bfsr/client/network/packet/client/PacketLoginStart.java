package net.bfsr.client.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketLoginStart implements PacketOut {
    private String playerName, password;
    private boolean registration;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeStringToBuffer(this.playerName);
        data.writeStringToBuffer(this.password);
        data.writeBoolean(registration);
    }
}