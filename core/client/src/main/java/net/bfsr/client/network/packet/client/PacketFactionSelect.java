package net.bfsr.client.network.packet.client;

import lombok.NoArgsConstructor;
import net.bfsr.faction.Faction;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
public class PacketFactionSelect implements PacketOut {
    private int faction;

    public PacketFactionSelect(Faction faction) {
        this.faction = faction.ordinal();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(faction);
    }
}