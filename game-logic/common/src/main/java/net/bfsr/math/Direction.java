package net.bfsr.math;

public enum Direction {
    FORWARD, LEFT, BACKWARD, RIGHT, STOP;

    public static final Direction[] VALUES = values();

    private static final Direction[] inverseDirections = new Direction[4];

    static {
        inverseDirections[0] = BACKWARD;
        inverseDirections[1] = RIGHT;
        inverseDirections[2] = FORWARD;
        inverseDirections[3] = LEFT;
    }

    public static Direction inverse(Direction direction) {
        return inverseDirections[direction.ordinal()];
    }

    public static Direction get(byte index) {
        return VALUES[index];
    }
}