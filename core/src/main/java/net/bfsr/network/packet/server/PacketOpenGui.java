package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.gui.GuiDestroyed;
import net.bfsr.client.gui.GuiFactionSelect;
import net.bfsr.core.Core;
import net.bfsr.network.EnumGui;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketOpenGui extends ServerPacket {

    private int gui;
    private String destroyer;

    public PacketOpenGui(EnumGui gui) {
        this.gui = gui.ordinal();
    }

    public PacketOpenGui(EnumGui gui, String destroyer) {
        this.gui = gui.ordinal();
        this.destroyer = destroyer;
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        gui = data.readInt();
        if (gui == 1) {
            destroyer = data.readStringFromBuffer(2048);
        }
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(gui);
        if (gui == 1) {
            data.writeStringToBuffer(destroyer);
        }
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        EnumGui enumGui = EnumGui.values()[gui];
        switch (enumGui) {
            case SelectFaction:
                Core.get().setCurrentGui(new GuiFactionSelect());
                return;
            case Destroyed:
                Core.get().setCurrentGui(new GuiDestroyed(destroyer));
        }
    }
}