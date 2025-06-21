package net.bfsr.engine.network.packet;

public final class CommonPacketRegistry {
    public static final int REGISTER_TCP = 0;
    public static final int REGISTER_UDP = 1;
    public static final int HANDSHAKE = 2;
    public static final int LOGIN = 3;
    public static final int LOGIN_DISCONNECT = 4;
    public static final int LOGIN_SUCCESS = 5;
    public static final int JOIN_GAME = 6;
    public static final int PING = 7;
    public static final int WORLD_SNAPSHOT = 8;
    public static final int ENTITY_SPAWN = 9;
    public static final int ENTITY_REMOVE = 10;
    public static final int PLAYER_SYNC_LOCAL_ID = 11;
    public static final int SYNC_TICK = 12;
}
