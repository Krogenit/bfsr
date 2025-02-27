package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.faction.Faction;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketFactionSelect extends PacketAdapter {
    private int faction;

    public PacketFactionSelect(Faction faction) {
        this.faction = faction.ordinal();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(faction);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        faction = data.readInt();
    }
}