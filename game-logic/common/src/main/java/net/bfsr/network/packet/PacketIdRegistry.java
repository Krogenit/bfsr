package net.bfsr.network.packet;

public final class PacketIdRegistry {
    /**
     * Common
     */
    public static final int CHAT_MESSAGE = 100;

    /**
     * Client
     */
    public static final int MOUSE_LEFT_CLICK = 101;
    public static final int MOUSE_LEFT_RELEASE = 102;
    public static final int MOUSE_POSITION = 103;
    public static final int SHIP_MOVE = 104;
    public static final int SHIP_STOP_MOVE = 105;
    public static final int CAMERA_POSITION = 106;

    public static final int FACTION_SELECT = 107;

    public static final int COMMAND = 108;
    public static final int PAUSE_GAME = 109;
    public static final int RESPAWN = 110;
    public static final int SHIP_CONTROL = 111;

    /**
     * Server
     */
    public static final int PLAYER_SET_SHIP = 112;

    public static final int OPEN_GUI = 113;

    public static final int ENTITY_SYNC_DAMAGE = 114;

    public static final int SHIP_SET_DESTROYING = 115;
    public static final int SHIP_INFO = 116;
    public static final int SHIP_SET_SPAWNED = 117;
    public static final int SHIP_SYNC_MOVE_DIRECTION = 118;

    public static final int COMPONENT_ADD = 119;
    public static final int COMPONENT_REMOVE = 120;
    public static final int SHIELD_REBUILD = 121;
    public static final int SHIELD_REBUILDING_TIME = 122;
    public static final int SHIELD_REMOVE = 123;
    public static final int WEAPON_SLOT_REMOVE = 124;
    public static final int WEAPON_SLOT_SHOOT = 125;

    public static final int EFFECT_HULL_CELL_DESTROY = 126;
}
