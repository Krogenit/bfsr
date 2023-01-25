package net.bfsr.client.model;

class Quad extends Primitive {
    static final int[] INDICES = {0, 1, 3, 3, 1, 2};

    Quad(int bufferCount) {
        super(bufferCount, 4, INDICES.length);
    }
}
