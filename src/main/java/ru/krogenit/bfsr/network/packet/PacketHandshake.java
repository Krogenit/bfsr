package ru.krogenit.bfsr.network.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.ClientPacket;
import ru.krogenit.bfsr.network.packet.server.PacketDisconnectLogin;
import ru.krogenit.bfsr.network.packet.server.PacketDisconnectPlay;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.network.server.SwitchEnumConnectionState;
import ru.krogenit.bfsr.network.status.EnumConnectionState;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketHandshake extends ClientPacket {

	private int version;
	private String host;
	private int port;
	private EnumConnectionState connectionState;

	@Override
	public void read(PacketBuffer data) throws IOException {
		this.version = data.readVarIntFromBuffer();
		this.host = data.readStringFromBuffer(256);
		this.port = data.readUnsignedShort();
		this.connectionState = EnumConnectionState.getConnectionStateByInt(data.readVarIntFromBuffer());
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeVarIntToBuffer(this.version);
		data.writeStringToBuffer(this.host);
		data.writeShort(this.port);
		data.writeVarIntToBuffer(this.connectionState.getInt());
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		if(server.isSinglePlayer()) networkManager.setConnectionState(connectionState);
		else {
			switch (SwitchEnumConnectionState.states[connectionState.ordinal()]) {
				case 1:
					networkManager.setConnectionState(EnumConnectionState.LOGIN);
					String message;

					if (version > 5) {
						message = "Outdated server! I'm still on Custom";
						networkManager.scheduleOutboundPacket(new PacketDisconnectLogin(message));
						networkManager.closeChannel(message);
					} else if (version < 5) {
						message = "Outdated client! Please use Custom";
						networkManager.scheduleOutboundPacket(new PacketDisconnectLogin(message));
						networkManager.closeChannel(message);
					}

					break;
				case 2:
					networkManager.setConnectionState(EnumConnectionState.STATUS);
					break;
				default:
					throw new UnsupportedOperationException("Invalid intention " + connectionState);
			}
		}
	}

	@Override
	public boolean hasPriority() {
		return true;
	}
}