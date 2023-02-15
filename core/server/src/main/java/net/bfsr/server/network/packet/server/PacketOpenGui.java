package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.EnumGui;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketOpenGui implements PacketOut {
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
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(gui);
        if (gui == 1) {
            data.writeStringToBuffer(destroyer);
        }
    }
}