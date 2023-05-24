package net.bfsr.engine.renderer.opengl;

public final class GL {
    public static final int
            GL_COLOR_INDEX = 0x1900,
            GL_STENCIL_INDEX = 0x1901,
            GL_DEPTH_COMPONENT = 0x1902,
            GL_RED = 0x1903,
            GL_GREEN = 0x1904,
            GL_BLUE = 0x1905,
            GL_ALPHA = 0x1906,
            GL_RGB = 0x1907,
            GL_RGBA = 0x1908,
            GL_LUMINANCE = 0x1909,
            GL_LUMINANCE_ALPHA = 0x190A;

    public static final int
            GL_R3_G3_B2 = 0x2A10,
            GL_RGB4 = 0x804F,
            GL_RGB5 = 0x8050,
            GL_RGB8 = 0x8051,
            GL_RGB10 = 0x8052,
            GL_RGB12 = 0x8053,
            GL_RGB16 = 0x8054,
            GL_RGBA2 = 0x8055,
            GL_RGBA4 = 0x8056,
            GL_RGB5_A1 = 0x8057,
            GL_RGBA8 = 0x8058,
            GL_RGB10_A2 = 0x8059,
            GL_RGBA12 = 0x805A,
            GL_RGBA16 = 0x805B,
            GL_TEXTURE_RED_SIZE = 0x805C,
            GL_TEXTURE_GREEN_SIZE = 0x805D,
            GL_TEXTURE_BLUE_SIZE = 0x805E,
            GL_TEXTURE_ALPHA_SIZE = 0x805F,
            GL_PROXY_TEXTURE_1D = 0x8063,
            GL_PROXY_TEXTURE_2D = 0x8064;

    public static final int
            GL_R8 = 0x8229,
            GL_R16 = 0x822A,
            GL_RG8 = 0x822B,
            GL_RG16 = 0x822C,
            GL_R16F = 0x822D,
            GL_R32F = 0x822E,
            GL_RG16F = 0x822F,
            GL_RG32F = 0x8230,
            GL_R8I = 0x8231,
            GL_R8UI = 0x8232,
            GL_R16I = 0x8233,
            GL_R16UI = 0x8234,
            GL_R32I = 0x8235,
            GL_R32UI = 0x8236,
            GL_RG8I = 0x8237,
            GL_RG8UI = 0x8238,
            GL_RG16I = 0x8239,
            GL_RG16UI = 0x823A,
            GL_RG32I = 0x823B,
            GL_RG32UI = 0x823C,
            GL_RG = 0x8227,
            GL_COMPRESSED_RED = 0x8225,
            GL_COMPRESSED_RG = 0x8226;

    public static final int
            GL_VENDOR = 0x1F00,
            GL_RENDERER = 0x1F01,
            GL_VERSION = 0x1F02,
            GL_EXTENSIONS = 0x1F03;

    public static final int
            GL_NEAREST = 0x2600,
            GL_LINEAR = 0x2601;

    public static final int
            GL_CLAMP_TO_EDGE = 0x812F,
            GL_CLAMP_TO_BORDER = 0x812D;

    public static final int
            GL_SCISSOR_TEST = 0xC11;

    public static final int
            GL_ZERO = 0,
            GL_ONE = 1,
            GL_SRC_COLOR = 0x300,
            GL_ONE_MINUS_SRC_COLOR = 0x301,
            GL_SRC_ALPHA = 0x302,
            GL_ONE_MINUS_SRC_ALPHA = 0x303,
            GL_DST_ALPHA = 0x304,
            GL_ONE_MINUS_DST_ALPHA = 0x305;

    public static final int
            GL_POINTS = 0x0,
            GL_LINES = 0x1,
            GL_LINE_LOOP = 0x2,
            GL_LINE_STRIP = 0x3,
            GL_TRIANGLES = 0x4,
            GL_TRIANGLE_STRIP = 0x5,
            GL_TRIANGLE_FAN = 0x6,
            GL_QUADS = 0x7,
            GL_QUAD_STRIP = 0x8,
            GL_POLYGON = 0x9;

    public static final int GL_REPEAT = 0x2901;
}