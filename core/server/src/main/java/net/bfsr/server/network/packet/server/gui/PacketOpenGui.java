package net.bfsr.server.network.packet.server.gui;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.GuiType;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketOpenGui implements PacketOut {
    private int gui;
    private String destroyer;

    public PacketOpenGui(GuiType gui) {
        this.gui = gui.ordinal();
    }

    public PacketOpenGui(GuiType gui, String destroyer) {
        this.gui = gui.ordinal();
        this.destroyer = destroyer;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(gui);
        if (gui == 1) {
            ByteBufUtils.writeString(data, destroyer);
        }
    }
}