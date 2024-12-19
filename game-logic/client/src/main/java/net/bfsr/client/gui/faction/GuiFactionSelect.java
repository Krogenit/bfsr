package net.bfsr.client.gui.faction;

import net.bfsr.client.Client;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.gui.component.TexturedRotatedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.client.PacketFactionSelect;

public class GuiFactionSelect extends Gui {
    public GuiFactionSelect() {
        add(new TexturedRectangle(TextureRegister.guiFactionSelect).atCenter(-1024 / 2, 0)
                .setSize(1024, 600));
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR).atCenter(-216 / 2, 200)
                .setSize(216, 216));
        add(new TexturedRectangle(TextureRegister.guiBfsrText2, 860, 80).atCenter(-860 / 2, 200));
        add(new Button(Lang.getString("gui.selectFaction.human"), () -> {
            Client.get().sendTCPPacket(new PacketFactionSelect(Faction.HUMAN));
            Client.get().closeGui();
        }).atCenter(-309 - 300 / 2, -230));
        add(new Button(Lang.getString("gui.selectFaction.saimon"), () -> {
            Client.get().sendTCPPacket(new PacketFactionSelect(Faction.SAIMON));
            Client.get().closeGui();
        }).atCenter(-1 - 300 / 2, -230));
        add(new Button(Lang.getString("gui.selectFaction.engi"), () -> {
            Client.get().sendTCPPacket(new PacketFactionSelect(Faction.ENGI));
            Client.get().closeGui();
        }).atCenter(309 - 300 / 2, -230));

        add(new GuiShip(TextureRegister.shipHumanSmall0, 120, 120).atCenter(-309 - 60, -70));
        add(new GuiShip(TextureRegister.shipSaimonSmall0, 170, 170).atCenter(-85, -70));
        add(new GuiShip(TextureRegister.shipEngiSmall0, 180, 180).atCenter(309 - 90, -70));

        Label label = new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.maintext"), 24);
        add(label.atCenter(-label.getWidth() / 2, 108));

        int discFontSize = 16;
        add(new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.humanDisc"), discFontSize).atCenter(-450, 72));
        add(new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.saimonDisc"), discFontSize).atCenter(-142, 72));
        add(new Label(Font.XOLONIUM_FT, Lang.getString("gui.selectFaction.engiDisc"), discFontSize).atCenter(166, 72));
    }

    private static class GuiShip extends TexturedRotatedRectangle {
        GuiShip(TextureRegister textureRegister, int width, int height) {
            super(textureRegister, width, height);
        }

        @Override
        public void update() {
            super.update();

            float rotSpeed = 0.04f;
            if (mouseHover) {
                setRotation(getRotation() + rotSpeed);

                if (getRotation() > MathUtils.TWO_PI) {
                    setRotation(getRotation() - MathUtils.TWO_PI);
                }
            } else {
                if (getRotation() > 0) {
                    if (getRotation() > Math.PI) {
                        float dif = MathUtils.TWO_PI - getRotation();
                        if (dif < 0.01f) dif = 0.01f;
                        setRotation(getRotation() + rotSpeed * dif);

                        if (getRotation() > MathUtils.TWO_PI) {
                            setRotation(0);
                        }
                    } else {
                        float dif = getRotation() - 0;
                        if (dif < 0.01f) dif = 0.01f;
                        setRotation(getRotation() - rotSpeed * dif);

                        if (getRotation() < 0) {
                            setRotation(0);
                        }
                    }
                }
            }
        }
    }
}