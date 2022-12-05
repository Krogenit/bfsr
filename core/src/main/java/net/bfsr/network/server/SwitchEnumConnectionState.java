package net.bfsr.network.server;

import net.bfsr.network.status.EnumConnectionState;

public class SwitchEnumConnectionState {
    public static final int[] states = new int[EnumConnectionState.values().length];

    static {
        try {
            states[EnumConnectionState.LOGIN.ordinal()] = 1;
        } catch (NoSuchFieldError ignored) {
        }

        try {
            states[EnumConnectionState.STATUS.ordinal()] = 2;
        } catch (NoSuchFieldError ignored) {
        }
    }
}
