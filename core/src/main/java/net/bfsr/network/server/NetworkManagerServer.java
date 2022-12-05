package net.bfsr.network.server;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.NetworkManager;
import net.bfsr.network.Packet;
import net.bfsr.network.packet.common.PacketKeepAlive;
import net.bfsr.network.packet.server.PacketDisconnectLogin;
import net.bfsr.network.packet.server.PacketLoginSuccess;
import net.bfsr.network.packet.server.PacketRemoveObject;
import net.bfsr.network.status.EnumConnectionState;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.util.List;

public class NetworkManagerServer extends NetworkManager {

    public static final int LOGIN_TIMEOUT = 600;

    private final MainServer server;
    private PlayerServer player;
    private final WorldServer world;
    @Getter
    @Setter
    private LoginState loginState = LoginState.HELLO;
    private int timeout;
    private String playerName, password;
    private boolean registration;

    private int networkTickCount;
    @Getter
    private int currentTimeInt;
    @Getter
    private long currentTime;
    private long lastNetworkTickCount;

    public NetworkManagerServer(MainServer server, WorldServer world, PlayerServer player) {
        super(false);
        this.server = server;
        this.world = world;
        this.player = player;
    }

    @Override
    protected void processPacket(Packet packet) {
        packet.processOnServerSide(this, server, world, player);
    }

    @Override
    protected void update() {
        switch (connectionState) {
            case PLAY:
                ++this.networkTickCount;

                if ((long) this.networkTickCount - this.lastNetworkTickCount > 40L) {
                    this.lastNetworkTickCount = this.networkTickCount;
                    this.currentTime = System.nanoTime() / 1000000L;
                    this.currentTimeInt = (int) this.currentTime;
                    this.scheduleOutboundPacket(new PacketKeepAlive(this.currentTimeInt));
                }

                // TODO: IDLE
//		        if () {
//		        	this.kickPlayerFromServer("You have been idle for too long!");
//		        }
                break;
            case LOGIN:
                if (this.loginState == LoginState.READY_TO_ACCEPT) {
                    this.initConnectionToPlayer();
                }

                if (this.timeout++ == LOGIN_TIMEOUT) {
                    disconnect("Took too long to log in");
                }
                break;
        }
    }

    @Override
    protected void onConnectionStateTransition(EnumConnectionState newState) {
        switch (connectionState) {
            case PLAY:
                if (newState != EnumConnectionState.PLAY) {
                    throw new IllegalStateException("Unexpected change in protocol!");
                }
                break;
            case LOGIN:
                if (this.loginState != LoginState.ACCEPTED && this.loginState != LoginState.HELLO) {
                    throw new RuntimeException("Unexpected change in protocol");
                }

                if (newState != EnumConnectionState.PLAY && newState != EnumConnectionState.LOGIN) {
                    throw new RuntimeException("Unexpected protocol " + newState);
                }
                break;
            case HANDSHAKING:
                if (newState != EnumConnectionState.LOGIN && newState != EnumConnectionState.STATUS) {
                    throw new UnsupportedOperationException("Invalid state " + newState);
                }
                break;
            case STATUS:
                if (newState != EnumConnectionState.STATUS) {
                    throw new UnsupportedOperationException("Unexpected change in protocol to " + newState);
                }
                break;
        }
    }

    @Override
    protected void onDisconnect(String reason) {
        switch (connectionState) {
            case PLAY:
                logger.info(player + " lost connection: " + reason);
                server.getWorld().removePlayer(player);
                server.getDataBase().saveUser(player);
                List<Ship> ships = player.getShips();
                for (Ship s : ships) {
                    s.setOwner(null);
                    s.setDead(true);
                    server.getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(s), s.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
                }

                if (server.isSinglePlayer()) {
                    logger.info("Stopping singleplayer server as player logged out");
                    server.stop();
                }
                break;
            case LOGIN:
                logger.info(getSocketAddress() + " lost connection: " + reason);
                break;
        }
    }

    public void initConnectionToPlayer() {
        if (server.isSinglePlayer()) {
            loginState = LoginState.ACCEPTED;
            scheduleOutboundPacket(new PacketLoginSuccess(playerName));
            server.getDataBase().authorizeUser(playerName, password);
            player = server.getDataBase().getPlayer(playerName);
            server.initializeConnectionToPlayer(this, player);
        } else {
            String message = server.getDataBase().authorizeUser(playerName, password);

            if (message == null) {
                player = server.getDataBase().getPlayer(playerName);
                if (server.getWorld().canJoin(player)) {
                    loginState = LoginState.ACCEPTED;
                    scheduleOutboundPacket(new PacketLoginSuccess(playerName));
                    server.initializeConnectionToPlayer(this, player);
                } else disconnect("Already in game");
            } else {
                disconnect(message);
            }
        }
    }

    private void disconnect(String reason) {
        try {
            logger.info("Disconnecting " + this.getSocketAddress() + ": " + reason);
            scheduleOutboundPacket(new PacketDisconnectLogin(reason));
            closeChannel(reason);
        } catch (Exception exception) {
            logger.error("Error whilst disconnecting player", exception);
        }
    }

    public void setPlayerLoginInfo(String playerName, String password, boolean registration) {
        this.playerName = playerName;
        this.password = password;
        this.registration = registration;
    }
}