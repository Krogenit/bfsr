package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.GuiDestroyed;
import net.bfsr.client.gui.GuiFactionSelect;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.EnumGui;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketOpenGui implements PacketIn {
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
    public void processOnClientSide(NetworkManagerClient networkManager) {
        EnumGui enumGui = EnumGui.values()[gui];
        if (enumGui == EnumGui.SelectFaction) {
            Core.get().setCurrentGui(new GuiFactionSelect());
        } else if (enumGui == EnumGui.Destroyed) {
            Core.get().setCurrentGui(new GuiDestroyed(destroyer));
        }
    }
}