package net.bfsr.client.font_new;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.client.render.VAO;
import org.lwjgl.opengl.GL11;

@RequiredArgsConstructor
@Getter
public class VAOListTexture {
    private final VAO vao;
    private final int callList;
    @Setter
    private int texture;
    @Setter
    private int vertexCount;

    public void clear() {
        vao.clear();
        GL11.glDeleteLists(callList, 1);
    }
}
