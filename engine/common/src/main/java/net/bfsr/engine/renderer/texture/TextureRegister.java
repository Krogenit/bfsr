package net.bfsr.engine.renderer.texture;

import lombok.Getter;
import net.bfsr.engine.renderer.constant.TextureFilter;
import net.bfsr.engine.renderer.constant.TextureWrap;
import net.bfsr.engine.util.PathHelper;

import java.nio.file.Path;

@Getter
public enum TextureRegister {
    shipHumanSmall0("texture/entity/ship/human_small0.png"),
    shipSaimonSmall0("texture/entity/ship/saimon_small0.png"),
    shipEngiSmall0("texture/entity/ship/engi_small0.png"),

    shieldSmall0("texture/component/shield/small0.png", TextureFilter.LINEAR),

    moduleReactor("texture/component/reactor/reactor.png"),
    moduleShield("texture/component/shield/shield.png"),
    moduleEngine("texture/component/engine/engine.png"),

    guiBfsrText2("texture/gui/bfsr_text2.png"),
    guiButtonBase("texture/gui/buttonBase.png"),
    guiLogoBFSR("texture/gui/logoBFSR.png"),
    guiSlider("texture/gui/slider.png"),
    guiAdd("texture/gui/add.png"),
    guiFactionSelect("texture/gui/selectFaction.png"),
    guiArmorPlate("texture/gui/armorPlate.png"),
    guiHudShip("texture/gui/hudship.png"),
    guiHudShipAdd("texture/gui/hudshipadd0.png"),
    guiEnergy("texture/gui/energy.png"),
    guiButtonControl("texture/gui/buttonControl.png"),
    guiShield("texture/gui/shield.png", TextureFilter.LINEAR),
    guiChat("texture/gui/chat.png"),

    particleBeam("texture/particle/beam.png", TextureFilter.LINEAR),
    particleBeamEffect("texture/particle/beameffect.png", TextureFilter.LINEAR),
    particleLight("texture/particle/light.png", TextureFilter.LINEAR),
    particleShipEngineBack("texture/particle/blue2.png", TextureFilter.LINEAR),
    particleJump("texture/particle/jump.png", TextureFilter.LINEAR),
    particleGarbage0("texture/particle/derbis0.png"),

    damageFire("texture/effect/fire.png", TextureWrap.REPEAT, TextureFilter.LINEAR);

    private final Path path;
    private final TextureWrap wrap;
    private final TextureFilter filter;

    TextureRegister(String path) {
        this(path, TextureWrap.CLAMP_TO_EDGE, TextureFilter.NEAREST);
    }

    TextureRegister(String path, TextureFilter filter) {
        this(path, TextureWrap.CLAMP_TO_EDGE, filter);
    }

    TextureRegister(String path, TextureWrap wrap, TextureFilter filter) {
        this.path = PathHelper.convertPath(path);
        this.wrap = wrap;
        this.filter = filter;
    }
}