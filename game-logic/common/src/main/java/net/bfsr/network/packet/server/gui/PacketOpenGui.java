package net.bfsr.network.packet.server.gui;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.GuiType;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketOpenGui extends PacketAdapter {
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

    @Override
    public void read(ByteBuf data) throws IOException {
        gui = data.readInt();
        if (gui == 1) {
            destroyer = ByteBufUtils.readString(data);
        }
    }

}