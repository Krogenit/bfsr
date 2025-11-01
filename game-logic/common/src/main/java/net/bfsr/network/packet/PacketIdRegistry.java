package net.bfsr.network.packet;

public final class PacketIdRegistry {
    /**
     * Common
     */
    public static final int CHAT_MESSAGE = 100;

    /**
     * Client
     */
    public static final int CLIENT_PLAYER_INPUT = 101;

    public static final int FACTION_SELECT = 107;

    public static final int COMMAND = 108;
    public static final int PAUSE_GAME = 109;
    public static final int RESPAWN = 110;
    public static final int SHIP_CONTROL = 111;

    public static final int SHIP_JUMP = 112;

    /**
     * Server
     */
    public static final int PLAYER_SET_SHIP = 1001;
    public static final int PLAYER_SET_CAMERA = 1016;

    public static final int OPEN_GUI = 1002;

    public static final int ENTITY_SYNC_DAMAGE = 1003;

    public static final int SHIP_SET_DESTROYING = 1004;
    public static final int SHIP_INFO = 1005;
    public static final int SHIP_SET_SPAWNED = 1006;
    public static final int SHIP_SYNC_MOVE_DIRECTION = 1007;

    public static final int COMPONENT_ADD = 1008;
    public static final int COMPONENT_REMOVE = 1009;
    public static final int SHIELD_REBUILD = 1010;
    public static final int SHIELD_REBUILDING_TIME = 1011;
    public static final int SHIELD_REMOVE = 1012;
    public static final int WEAPON_SLOT_REMOVE = 1013;
    public static final int WEAPON_SLOT_SHOOT = 1014;

    public static final int EFFECT_HULL_CELL_DESTROY = 1015;
}
