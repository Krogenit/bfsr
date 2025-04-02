package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.FACTION_SELECT)
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
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        faction = data.readInt();
    }
}