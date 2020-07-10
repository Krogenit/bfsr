package ru.krogenit.bfsr.network.packet;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.ClientPacket;
import ru.krogenit.bfsr.network.server.LoginState;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketLoginStart extends ClientPacket {

	private String playerName, password;
	private boolean registration;

	@Override
	public void read(PacketBuffer data) throws IOException {
		this.playerName = data.readStringFromBuffer(32767);
		this.password = data.readStringFromBuffer(32767);
		this.registration = data.readBoolean();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeStringToBuffer(this.playerName);
		data.writeStringToBuffer(this.password);
		data.writeBoolean(registration);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		if (networkManager.getLoginState() != LoginState.HELLO) {
			throw new RuntimeException("Unexpected hello packet");
		}

		networkManager.setPlayerLoginInfo(playerName, password, registration);
		networkManager.setLoginState(LoginState.READY_TO_ACCEPT);
	}
}