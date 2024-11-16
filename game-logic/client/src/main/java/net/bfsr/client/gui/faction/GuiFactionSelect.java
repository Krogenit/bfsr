package net.bfsr.client.gui.faction;

import net.bfsr.client.Client;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.gui.component.TexturedRotatedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.client.PacketFactionSelect;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class GuiFactionSelect extends Gui {
    private final List<GuiObject> ships = new ArrayList<>(3);

    public GuiFactionSelect() {
        add(new TexturedRectangle(TextureRegister.guiFactionSelect).atCenter(-1024 / 2, -600 / 2)
                .setSize(1024, 600));
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR).atCenter(-216 / 2, -200 - 216 / 2)
                .setSize(216, 216));
        add(new TexturedRectangle(TextureRegister.guiBfsrText2, 860, 80).atCenter(-860 / 2, -200 - 80 / 2));
        add(new Button(Lang.getString("gui.selectFaction.human"), () -> {
            Client.get().sendTCPPacket(new PacketFactionSelect(Faction.HUMAN));
            Client.get().closeGui();
        }).atCenter(-309 - 300 / 2, 230 - 50 / 2));
        add(new Button(Lang.getString("gui.selectFaction.saimon"), () -> {
            Client.get().sendTCPPacket(new PacketFactionSelect(Faction.SAIMON));
            Client.get().closeGui();
        }).atCenter(-1 - 300 / 2, 230 - 50 / 2));
        add(new Button(Lang.getString("gui.selectFaction.engi"), () -> {
            Client.get().sendTCPPacket(new PacketFactionSelect(Faction.ENGI));
            Client.get().closeGui();
        }).atCenter(309 - 300 / 2, 230 - 50 / 2));

        ships.add(new TexturedRotatedRectangle(TextureRegister.shipHumanSmall0, 120, 120).atCenter(-309 - 60, 70 - 60));
        ships.add(new TexturedRotatedRectangle(TextureRegister.shipSaimonSmall0, 170, 170).atCenter(-85, 70 - 85));
        ships.add(new TexturedRotatedRectangle(TextureRegister.shipEngiSmall0, 180, 180).atCenter(309 - 90, 70 - 90));

        for (int i = 0; i < ships.size(); i++) {
            add(ships.get(i));
        }

        Label label = new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.maintext"), 24);
        add(label.atCenter(-label.getWidth() / 2, -96));

        int discFontSize = 16;
        add(new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.humanDisc"), discFontSize).atCenter(-450, -64));
        add(new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.saimonDisc"), discFontSize).atCenter(-142, -64));
        add(new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.engiDisc"), discFontSize).atCenter(166, -64));
    }

    private void updateRot(GuiObject guiObject, Vector2f mousePosition) {
        float rotSpeed = 0.04f;

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
        for (int i = 0; i < ships.size(); i++) {
            updateRot(ships.get(i), position);
        }
    }
}