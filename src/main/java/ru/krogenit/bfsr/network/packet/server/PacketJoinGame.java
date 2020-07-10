package ru.krogenit.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketJoinGame extends ServerPacket {

	private long seed;

	public void read(PacketBuffer data) throws IOException {
		this.seed = data.readLong();
	}

	public void write(PacketBuffer data) throws IOException {
		data.writeLong(seed);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		Core core = Core.getCore();
		core.setCurrentGui(null);
		core.getWorld().setSeed(seed);
	}
}