package net.bfsr.engine.renderer.texture;

import lombok.Getter;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.util.PathHelper;

import java.nio.file.Path;

@Getter
public enum TextureRegister {
    shipHumanSmall0("texture/entity/ship/human_small0.png"),
    shipSaimonSmall0("texture/entity/ship/saimon_small0.png"),
    shipEngiSmall0("texture/entity/ship/engi_small0.png"),

    shieldSmall0("texture/component/shield/small0.png", GL.GL_LINEAR),

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
    guiShield("texture/gui/shield.png", GL.GL_LINEAR),
    guiChat("texture/gui/chat.png"),

    particleBeam("texture/particle/beam.png", GL.GL_LINEAR),
    particleBeamEffect("texture/particle/beameffect.png", GL.GL_LINEAR),
    particleLight("texture/particle/light.png", GL.GL_LINEAR),
    particleShipEngineBack("texture/particle/blue2.png", GL.GL_LINEAR),
    particleJump("texture/particle/jump.png", GL.GL_LINEAR),
    particleGarbage0("texture/particle/derbis0.png"),

    damageFire("texture/effect/fire.png", GL.GL_REPEAT, GL.GL_LINEAR);

    private final Path path;
    private final int wrap;
    private final int filter;

    TextureRegister(String path) {
        this(path, GL.GL_CLAMP_TO_EDGE, GL.GL_NEAREST);
    }

    TextureRegister(String path, int filter) {
        this(path, GL.GL_CLAMP_TO_EDGE, filter);
    }

    TextureRegister(String path, int wrap, int filter) {
        this.path = PathHelper.convertPath(path);
        this.wrap = wrap;
        this.filter = filter;
    }
}