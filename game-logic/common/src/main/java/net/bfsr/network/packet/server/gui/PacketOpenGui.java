package net.bfsr.network.packet.server.gui;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.util.ByteBufUtils;
import net.bfsr.network.GuiType;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.OPEN_GUI)
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
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        gui = data.readInt();
        if (gui == 1) {
            destroyer = ByteBufUtils.readString(data);
        }
    }
}