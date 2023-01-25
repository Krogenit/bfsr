package net.bfsr.client.render.texture;

import net.bfsr.client.render.texture.dds.DDSFile;
import net.bfsr.util.PathHelper;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;

public final class TextureLoader {
    private static final EnumMap<TextureRegister, Texture> LOADED_TEXTURES = new EnumMap<>(TextureRegister.class);
    public static Texture dummyTexture;

    public static void init() {
        createDummyTexture();
    }

    private static void createDummyTexture() {
        int width = 4;
        int height = 4;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = stack.malloc(width * height * 4);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    byteBuffer.put((byte) 32);
                    byteBuffer.put((byte) 32);
                    byteBuffer.put((byte) 32);
                }
            }

            dummyTexture = createTexture(width, height, byteBuffer.flip(), 3, false);
        }
    }

    public static Texture getTexture(TextureRegister texture, boolean createMips) {
        if (texture == null) return null;
        if (!LOADED_TEXTURES.containsKey(texture)) loadPngTexture(texture, createMips);

        return LOADED_TEXTURES.get(texture);
    }

    public static Texture getTexture(TextureRegister texture) {
        return getTexture(texture, true);
    }

    private static void loadDDSTexture(TextureRegister name) {
        if (LOADED_TEXTURES.containsKey(name)) {
            LOADED_TEXTURES.get(name);
            return;
        }

        DDSFile dds = null;

        try {
            InputStream in = new FileInputStream(new File(PathHelper.texture, name.getPath().replace("/", File.separator) + ".dds"));
            dds = new DDSFile(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (dds == null) throw new RuntimeException("Failed to load a texture file " + name);

        Texture texture = generateTexture(dds);
        LOADED_TEXTURES.put(name, texture);
    }

    private static void loadPngTexture(TextureRegister name, boolean createMips) {
        ByteBuffer image;
        int width, height, channels;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(false);
            image = STBImage.stbi_load(new File(PathHelper.texture, name.getPath().replace("/", File.separator) + ".png").toString(), w, h, comp, 0);
            if (image == null) {
                throw new RuntimeException("Failed to load a texture file!" + System.lineSeparator() + STBImage.stbi_failure_reason());
            }

            channels = comp.get();
            width = w.get();
            height = h.get();
        }

        Texture texture = createTexture(width, height, image, channels, createMips);
        if (name.getNumberOfRows() != 0) texture.setNumberOfRows(name.getNumberOfRows());

        LOADED_TEXTURES.put(name, texture);
    }

    private static Texture generateTexture(DDSFile dds) {
        Texture texture = new Texture(dds.getWidth(), dds.getHeight());

        GL45C.glTextureStorage2D(texture.getId(), dds.getMipMapCount() + 1, dds.getDXTFormat(), dds.getWidth(), dds.getHeight());
        GL45C.glCompressedTextureSubImage2D(texture.getId(), 0, 0, 0, dds.getWidth(), dds.getHeight(), dds.getDXTFormat(), dds.getBuffer());
        int mipMapCount = dds.getMipMapCount();
        int textureMagFilter = GL11C.GL_LINEAR;
        int textureMinFilter = (mipMapCount > 0) ? GL11C.GL_LINEAR_MIPMAP_LINEAR : GL11C.GL_LINEAR;
        if (mipMapCount > 0) {
            GL45C.glTextureParameteri(texture.getId(), GL12C.GL_TEXTURE_MAX_LEVEL, mipMapCount);
            GL45C.glTextureParameterf(texture.getId(), GL12C.GL_TEXTURE_MIN_LOD, 0.0f);
            GL45C.glTextureParameterf(texture.getId(), GL12C.GL_TEXTURE_MAX_LOD, mipMapCount);
            GL45C.glTextureParameterf(texture.getId(), GL14C.GL_TEXTURE_LOD_BIAS, 0.0f);

            int width = dds.getWidth();
            int height = dds.getHeight();
            for (int i = 0; i < mipMapCount; i++) {
                width /= 2;
                height /= 2;
                ByteBuffer mipmapBuffer = dds.getMipMapLevel(i);
                GL45C.glCompressedTextureSubImage2D(texture.getId(), i + 1, 0, 0, width, height, dds.getDXTFormat(), mipmapBuffer);
            }
        }

        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_MAG_FILTER, textureMagFilter);
        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_MIN_FILTER, textureMinFilter);
        GL45C.glTextureParameterf(texture.getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, GL11C.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));

        return texture;
    }

    private static Texture createTexture(int width, int height, ByteBuffer image, int channels, boolean createMips) {
        Texture texture = new Texture(width, height);

        int internalFormat;
        int format;
        if (channels == 1) {
            internalFormat = GL30.GL_R8;
            format = GL11.GL_R;
        } else if (channels == 2) {
            internalFormat = GL30.GL_RG8;
            format = GL30.GL_RG;
        } else if (channels == 3) {
            internalFormat = GL11.GL_RGB8;
            format = GL11.GL_RGB;
        } else if (channels == 4) {
            internalFormat = GL11.GL_RGBA8;
            format = GL11.GL_RGBA;
        } else {
            throw new RuntimeException("Unsupported image channels " + channels);
        }

        GL45C.glTextureStorage2D(texture.getId(), 1, internalFormat, width, height);
        GL45C.glTextureSubImage2D(texture.getId(), 0, 0, 0, width, height, format, GL11.GL_UNSIGNED_BYTE, image);

        if (createMips) GL45C.glGenerateTextureMipmap(texture.getId());

        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MIN_FILTER, createMips ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL45C.glTextureParameterf(texture.getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));

        return texture;
    }
}
