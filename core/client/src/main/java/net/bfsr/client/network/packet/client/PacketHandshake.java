package net.bfsr.client.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.client.network.EnumConnectionState;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketHandshake implements PacketOut {
    private int version;
    private String host;
    private int port;
    private EnumConnectionState connectionState;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeVarIntToBuffer(this.version);
        data.writeStringToBuffer(this.host);
        data.writeShort(this.port);
        data.writeVarIntToBuffer(this.connectionState.getInt());
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}