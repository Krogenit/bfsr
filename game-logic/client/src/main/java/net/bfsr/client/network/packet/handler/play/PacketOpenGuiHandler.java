package net.bfsr.client.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.gui.faction.GuiFactionSelect;
import net.bfsr.client.gui.state.GuiDestroyed;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.GuiType;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.gui.PacketOpenGui;

import java.net.InetSocketAddress;

public class PacketOpenGuiHandler extends PacketHandler<PacketOpenGui, NetworkSystem> {
    @Override
    public void handle(PacketOpenGui packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GuiType guiType = GuiType.values()[packet.getGui()];
        if (guiType == GuiType.SELECT_FACTION) {
            Core.get().openGui(new GuiFactionSelect());
        } else if (guiType == GuiType.DESTROYED) {
            Core.get().openGui(new GuiDestroyed(packet.getDestroyer()));
        }
    }
}