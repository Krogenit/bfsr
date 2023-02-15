package net.bfsr.server.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.EnumConnectionState;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;
import net.bfsr.server.network.packet.server.PacketDisconnectLogin;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketHandshake implements PacketIn {
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
    public void processOnServerSide(NetworkManagerServer networkManager) {
        if (MainServer.getInstance().isSinglePlayer()) networkManager.setConnectionState(connectionState);
        else {
            switch (connectionState) {
                case LOGIN:
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
                case STATUS:
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