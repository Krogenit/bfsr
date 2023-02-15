package net.bfsr.server.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.LoginState;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketLoginStart implements PacketIn {
    private String playerName, password;
    private boolean registration;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.playerName = data.readStringFromBuffer(32767);
        this.password = data.readStringFromBuffer(32767);
        this.registration = data.readBoolean();
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        if (networkManager.getLoginState() != LoginState.HELLO) {
            throw new RuntimeException("Unexpected hello packet");
        }

        networkManager.setPlayerLoginInfo(playerName, password, registration);
        networkManager.setLoginState(LoginState.READY_TO_ACCEPT);
    }
}