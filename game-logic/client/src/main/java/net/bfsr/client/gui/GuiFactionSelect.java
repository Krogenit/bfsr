package net.bfsr.client.gui;

import net.bfsr.client.Core;
import net.bfsr.client.font.StringObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.client.PacketFactionSelect;
import org.joml.Vector2f;

public class GuiFactionSelect extends Gui {
    private int shipsStartIndex;

    @Override
    protected void initElements() {
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiFactionSelect).atCenter(-1024 / 2, -600 / 2).setSize(1024, 600));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiLogoBFSR).atCenter(-216 / 2, -200 - 216 / 2).setSize(216, 216));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiBfsrText2).atCenter(-860 / 2, -200 - 80 / 2).setSize(860, 80));
        registerGuiObject(new Button(Lang.getString("gui.selectFaction.human"), () -> {
            Core.get().sendTCPPacket(new PacketFactionSelect(Faction.HUMAN));
            Core.get().setCurrentGui(null);
        }).atCenter(-309 - 300 / 2, 230 - 50 / 2));
        registerGuiObject(new Button(Lang.getString("gui.selectFaction.saimon"), () -> {
            Core.get().sendTCPPacket(new PacketFactionSelect(Faction.SAIMON));
            Core.get().setCurrentGui(null);
        }).atCenter(-1 - 300 / 2, 230 - 50 / 2));
        registerGuiObject(new Button(Lang.getString("gui.selectFaction.engi"), () -> {
            Core.get().sendTCPPacket(new PacketFactionSelect(Faction.ENGI));
            Core.get().setCurrentGui(null);
        }).atCenter(309 - 300 / 2, 230 - 50 / 2));

        shipsStartIndex = guiObjects.size();
        registerGuiObject(new TexturedGuiObject(TextureRegister.shipHumanSmall0).centered().atCenter(-309 - 60, 70 - 60).setSize(120, 120));
        registerGuiObject(new TexturedGuiObject(TextureRegister.shipSaimonSmall0).centered().atCenter(-85, 70 - 85).setSize(170, 170));
        registerGuiObject(new TexturedGuiObject(TextureRegister.shipEngiSmall0).centered().atCenter(309 - 90, 70 - 90).setSize(180, 180));

        StringObject stringObject = new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.maintext"), 24).compile();
        registerGuiObject(stringObject.atCenter(-stringObject.getWidth() / 2, -96));

        int discFontSize = 16;
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.humanDisc"), discFontSize).compile().atCenter(-450, -64));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.saimonDisc"), discFontSize).compile().atCenter(-142, -64));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.engiDisc"), discFontSize).compile().atCenter(166, -64));
    }

    private void updateRot(TexturedGuiObject guiObject, Vector2f mousePosition) {
        float rotSpeed = 0.04f;

        guiObject.setLastRotation(guiObject.getRotation());
        if (guiObject.isIntersects(mousePosition)) {
            guiObject.setRotation(guiObject.getRotation() + rotSpeed);

            if (guiObject.getRotation() > MathUtils.TWO_PI) {
                guiObject.setRotation(guiObject.getRotation() - MathUtils.TWO_PI);
            }
        } else {
            if (guiObject.getRotation() > 0) {
                if (guiObject.getRotation() > Math.PI) {
                    float dif = MathUtils.TWO_PI - guiObject.getRotation();
                    if (dif < 0.01f) dif = 0.01f;
                    guiObject.setRotation(guiObject.getRotation() + rotSpeed * dif);

                    if (guiObject.getRotation() > MathUtils.TWO_PI) {
                        guiObject.setRotation(0);
                    }
                } else {
                    float dif = guiObject.getRotation() - 0;
                    if (dif < 0.01f) dif = 0.01f;
                    guiObject.setRotation(guiObject.getRotation() - rotSpeed * dif);

                    if (guiObject.getRotation() < 0) {
                        guiObject.setRotation(0);
                    }
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();

        Vector2f position = Engine.mouse.getPosition();
        for (int i = 0; i < 3; i++) {
            TexturedGuiObject guiObject = (TexturedGuiObject) guiObjects.get(shipsStartIndex + i);
            updateRot(guiObject, position);
        }
    }
}