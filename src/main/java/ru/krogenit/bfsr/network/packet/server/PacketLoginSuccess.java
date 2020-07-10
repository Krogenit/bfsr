package ru.krogenit.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;
import ru.krogenit.bfsr.network.status.EnumConnectionState;
import ru.krogenit.bfsr.world.WorldClient;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketLoginSuccess extends ServerPacket {

	private String playerName;

	@Override
	public void read(PacketBuffer data) throws IOException {
		this.playerName = data.readStringFromBuffer(32767);
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeStringToBuffer(this.playerName);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		networkManager.setConnectionState(EnumConnectionState.PLAY);
		Core.getCore().addFutureTask(() -> Core.getCore().setWorld(new WorldClient()));
	}

	@Override
	public boolean hasPriority() {
		return true;
	}
}