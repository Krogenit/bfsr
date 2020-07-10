package ru.krogenit.bfsr.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketChatMessage extends Packet {

	private String message;

	@Override
	public void read(PacketBuffer data) throws IOException {
		message = data.readStringFromBuffer(2048);
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeStringToBuffer(message);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		Core.getCore().getGuiInGame().addChatMessage(message);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		server.getNetworkSystem().sendPacketToAll(new PacketChatMessage(message));
	}
}