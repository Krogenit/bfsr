package ru.krogenit.bfsr.client.font;

import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.util.PathHelper;

import java.io.File;

@Deprecated
public class FontRegistry {
    public static final FontType ARIAL = new FontType(TextureLoader.getTexture(TextureRegister.fontArialNew, false).getId(), new File(PathHelper.font, "arial_new.fnt"));
    public static final FontType BAHNSCHRIFT = new FontType(TextureLoader.getTexture(TextureRegister.fontBahnschrift, false).getId(), new File(PathHelper.font, "bahnschrift.fnt"));

    public static final FontType CONSOLA = new FontType(TextureLoader.getTexture(TextureRegister.fontConsola, false).getId(), new File(PathHelper.font, "consola.fnt"));
    public static final FontType NASALIZATION_RG = new FontType(TextureLoader.getTexture(TextureRegister.fontNasalization_rg, false).getId(), new File(PathHelper.font, "nasalization-rg.fnt"));

    public static final FontType CONTHRAX = new FontType(TextureLoader.getTexture(TextureRegister.fontConthrax, false).getId(), new File(PathHelper.font, "conthrax-sb.fnt"));
    public static final FontType XOLONIUM = new FontType(TextureLoader.getTexture(TextureRegister.fontXolonium, false).getId(), new File(PathHelper.font, "xolonium-regular.fnt"));
}
