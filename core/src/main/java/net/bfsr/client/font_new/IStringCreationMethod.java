package net.bfsr.client.font_new;

@FunctionalInterface
public interface IStringCreationMethod {
    void render(GLString glString, VAOListTexture vaoListTexture);
}
