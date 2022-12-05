package net.bfsr.client.font_new;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GLString {
    private final List<VAOListTexture> vaoList = new ArrayList<>(1);
    @Setter
    private int width, height;
    @Setter
    private int VAOCount;

    public void add(VAOListTexture vao) {
        vaoList.add(vao);
    }

    public void clear() {
        for (int i = 0; i < vaoList.size(); i++) {
            vaoList.get(i).clear();
        }
        vaoList.clear();
    }
}
