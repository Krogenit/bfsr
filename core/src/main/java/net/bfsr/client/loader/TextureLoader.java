package net.bfsr.client.loader;

import net.bfsr.client.texture.Texture;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.util.PathHelper;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class TextureLoader {
    private static final Map<TextureRegister, Texture> LOADED_TEXTURES = new HashMap<>();

    public static Texture getTexture(TextureRegister texture, boolean createMips) {
        if (texture == null) return null;
        if (!LOADED_TEXTURES.containsKey(texture)) loadPngTexture(texture, createMips);

        return LOADED_TEXTURES.get(texture);
    }

    public static Texture getTexture(TextureRegister texture) {
        return getTexture(texture, true);
    }

    public static Texture loadDDSTexture(TextureRegister name) {
        if (LOADED_TEXTURES.containsKey(name)) return LOADED_TEXTURES.get(name);

        DDSFile dds = null;

        try {
            InputStream in = new FileInputStream(new File(PathHelper.texture, name.getPath().replace("/", File.separator) + ".png"));
            dds = new DDSFile(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dds == null) throw new RuntimeException("Failed to load a texture file " + name);

        Texture texture = generateDDSTexture(dds);
        LOADED_TEXTURES.put(name, texture);
        return texture;
    }

    public static void loadPngTexture(TextureRegister name, boolean createMips) {
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

        Texture texture = createOpenGLTexture(width, height, image, channels, createMips);
        if (name.getNumberOfRows() != 0) texture.setNumberOfRows(name.getNumberOfRows());

        LOADED_TEXTURES.put(name, texture);
    }

    public static Texture generateDDSTexture(DDSFile dds) {
        Texture texture = new Texture(dds.getWidth(), dds.getHeight());
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
        GL13.glCompressedTexImage2D(GL11.GL_TEXTURE_2D, 0, dds.getDXTFormat(), dds.getWidth(), dds.getHeight(), 0, dds.getBuffer());

        if (dds.getMipMapCount() > 0) {
            int maxMipMapLevel = 5;
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, maxMipMapLevel);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, (float) maxMipMapLevel);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);

            int width = dds.getWidth();
            int height = dds.getHeight();
            for (int i = 0; i < maxMipMapLevel; i++) {
                width /= 2;
                height /= 2;
                ByteBuffer mipmapBuffer = dds.getMipMapLevel(i);
                int mipMapLevel = i + 1;
                GL13.glCompressedTexImage2D(GL11.GL_TEXTURE_2D, mipMapLevel, dds.getDXTFormat(), width, height, 0, mipmapBuffer);
            }
        } else GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float amount = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
        }

        return texture;
    }

    public static Texture createOpenGLTexture(int width, int height, ByteBuffer image, int channels, boolean createMips) {
        Texture texture = new Texture(width, height);

        int internalFormat;
        int format;
        switch (channels) {
            case 1:
                internalFormat = format = GL11.GL_R;
                break;
            case 2:
                internalFormat = format = GL30.GL_RG;
                break;
            case 3:
                internalFormat = format = GL11.GL_RGB;
                break;
            case 4:
                internalFormat = format = GL11.GL_RGBA;
                break;
            default:
                throw new RuntimeException("Unsupported image channels " + channels);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL11.GL_UNSIGNED_BYTE, image);

        if (createMips) GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, createMips ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float amount = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
        }

        return texture;
    }
}
