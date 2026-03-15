package net.bfsr.client.assets;

import lombok.Getter;
import net.bfsr.engine.renderer.constant.TextureFilter;
import net.bfsr.engine.renderer.constant.TextureWrap;
import net.bfsr.engine.renderer.texture.TextureData;
import net.bfsr.engine.util.PathHelper;

@Getter
public enum TextureRegister {
    moduleReactor("texture/component/reactor/reactor.png"),
    moduleShield("texture/component/shield/shield.png"),
    moduleEngine("texture/component/engine/engine.png"),

    guiLogoBFSR("texture/gui/logoBFSR.png"),
    guiShield("texture/gui/shield.png", TextureFilter.LINEAR),

    beam("texture/particle/beam.png", TextureFilter.LINEAR),
    light("texture/particle/light.png", TextureFilter.LINEAR),
    shipEngineBack("texture/particle/blue2.png", TextureFilter.LINEAR),
    jump("texture/particle/jump.png", TextureFilter.LINEAR),

    damageFire("texture/effect/fire.png", TextureWrap.REPEAT, TextureFilter.LINEAR);

    private final TextureData textureData;

    TextureRegister(String path) {
        this(path, TextureWrap.CLAMP_TO_EDGE, TextureFilter.NEAREST);
    }

    TextureRegister(String path, TextureFilter filter) {
        this(path, TextureWrap.CLAMP_TO_EDGE, filter);
    }

    TextureRegister(String path, TextureWrap wrap, TextureFilter filter) {
        textureData = new TextureData(PathHelper.convertPath(path), wrap, filter);
    }
}