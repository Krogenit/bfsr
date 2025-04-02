package net.bfsr.network.packet;

public final class PacketIdRegistry {
    /**
     * Common
     */
    public static final int CHAT_MESSAGE = 11;

    /**
     * Client
     */
    public static final int MOUSE_LEFT_CLICK = 12;
    public static final int MOUSE_LEFT_RELEASE = 13;
    public static final int MOUSE_POSITION = 14;
    public static final int SHIP_MOVE = 15;
    public static final int SHIP_STOP_MOVE = 16;
    public static final int CAMERA_POSITION = 17;

    public static final int FACTION_SELECT = 18;

    public static final int COMMAND = 19;
    public static final int PAUSE_GAME = 20;
    public static final int RESPAWN = 21;
    public static final int SHIP_CONTROL = 22;

    /**
     * Server
     */
    public static final int PLAYER_SET_SHIP = 23;

    public static final int OPEN_GUI = 24;

    public static final int ENTITY_SYNC_DAMAGE = 25;

    public static final int SHIP_SET_DESTROYING = 26;
    public static final int SHIP_INFO = 27;
    public static final int SHIP_SET_SPAWNED = 28;
    public static final int SHIP_SYNC_MOVE_DIRECTION = 29;

    public static final int COMPONENT_ADD = 30;
    public static final int COMPONENT_REMOVE = 31;
    public static final int SHIELD_REBUILD = 32;
    public static final int SHIELD_REBUILDING_TIME = 33;
    public static final int SHIELD_REMOVE = 34;
    public static final int WEAPON_SLOT_REMOVE = 35;
    public static final int WEAPON_SLOT_SHOOT = 36;

    public static final int EFFECT_BULLET_HIT_SHIP = 37;
    public static final int EFFECT_HULL_CELL_DESTROY = 38;
}
