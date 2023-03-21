package net.bfsr.client.network.packet.server.gui;

import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.GuiDestroyed;
import net.bfsr.client.gui.GuiFactionSelect;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.GuiType;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

public class PacketOpenGui implements PacketIn {
    private int gui;
    private String destroyer;

    @Override
    public void read(ByteBuf data) throws IOException {
        gui = data.readInt();
        if (gui == 1) {
            destroyer = ByteBufUtils.readString(data);
        }
    }

    @Override
    public void processOnClientSide() {
        GuiType guiType = GuiType.values()[gui];
        if (guiType == GuiType.SELECT_FACTION) {
            Core.get().setCurrentGui(new GuiFactionSelect());
        } else if (guiType == GuiType.DESTROYED) {
            Core.get().setCurrentGui(new GuiDestroyed(destroyer));
        }
    }
}