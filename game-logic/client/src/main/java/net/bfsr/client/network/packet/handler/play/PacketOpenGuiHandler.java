package net.bfsr.client.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.gui.faction.GuiFactionSelect;
import net.bfsr.client.gui.state.GuiDestroyed;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.GuiType;
import net.bfsr.network.packet.server.gui.PacketOpenGui;

import java.net.InetSocketAddress;

public class PacketOpenGuiHandler extends PacketHandler<PacketOpenGui, NetworkSystem> {
    @Override
    public void handle(PacketOpenGui packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GuiType guiType = GuiType.values()[packet.getGui()];
        Client client = Client.get();
        if (guiType == GuiType.SELECT_FACTION) {
            client.openGui(new GuiFactionSelect());
        } else if (guiType == GuiType.DESTROYED) {
            client.openGui(new GuiDestroyed(packet.getDestroyer()));
        }
    }
}