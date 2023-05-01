package net.bfsr.damage;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class DamageMask {
    public int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE;
    public int maxX, maxY;
    private int width, height;
    private byte[] data;

    public DamageMask(int width, int height, byte[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public DamageMask(int width, int height) {
        this.width = width;
        this.height = height;
        Arrays.fill(this.data = new byte[width * height], (byte) 255);
    }

    public void reset() {
        x = Integer.MAX_VALUE;
        y = Integer.MAX_VALUE;
        maxX = 0;
        maxY = 0;
    }

    public byte[] copy() {
        int size = width * height;
        byte[] copy = new byte[size];
        System.arraycopy(data, 0, copy, 0, size);
        return copy;
    }

    public boolean dirty() {
        return maxX > 0 && maxY > 0;
    }
}