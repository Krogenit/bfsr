package ru.krogenit.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.client.gui.GuiDestroyed;
import ru.krogenit.bfsr.client.gui.GuiFactionSelect;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.network.EnumGui;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

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
		if(gui == 1) {
			destroyer = data.readStringFromBuffer(2048);
		}
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(gui);
		if(gui == 1) {
			data.writeStringToBuffer(destroyer);
		}
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		EnumGui enumGui = EnumGui.values()[gui];
		switch (enumGui) {
			case SelectFaction:
				Core.getCore().setCurrentGui(new GuiFactionSelect());
				return;
			case Destroyed:
				Core.getCore().setCurrentGui(new GuiDestroyed(destroyer));
		}
	}
}