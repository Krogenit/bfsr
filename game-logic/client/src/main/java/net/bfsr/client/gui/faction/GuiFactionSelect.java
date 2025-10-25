package net.bfsr.client.gui.faction;

import net.bfsr.client.Client;
import net.bfsr.client.font.FontType;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.gui.component.TexturedRotatedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.client.PacketFactionSelect;

public class GuiFactionSelect extends Gui {
    public GuiFactionSelect() {
        Client client = Client.get();
        LanguageManager languageManager = client.getLanguageManager();
        add(new TexturedRectangle(TextureRegister.guiFactionSelect).atCenter(0, 0)
                .setSize(1024, 600));
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR).atCenter(0, 200)
                .setSize(216, 216));
        add(new TexturedRectangle(TextureRegister.guiBfsrText2, 860, 80).atCenter(0, 200));
        add(new Button(languageManager.getString("gui.selectFaction.human"), (mouseX, mouseY) -> {
            client.sendTCPPacket(new PacketFactionSelect(Faction.HUMAN));
            client.closeGui();
        }).atCenter(-309, -230));
        add(new Button(languageManager.getString("gui.selectFaction.saimon"), (mouseX, mouseY) -> {
            client.sendTCPPacket(new PacketFactionSelect(Faction.SAIMON));
            client.closeGui();
        }).atCenter(-1, -230));
        add(new Button(languageManager.getString("gui.selectFaction.engi"), (mouseX, mouseY) -> {
            client.sendTCPPacket(new PacketFactionSelect(Faction.ENGI));
            client.closeGui();
        }).atCenter(309, -230));

        add(new GuiShip(TextureRegister.shipHumanSmall0, 103, 118).atCenter(-309, -70));
        float scale = 1.33f;
        add(new GuiShip(TextureRegister.shipSaimonSmall0, (int) (100 * scale), (int) (65 * scale)).atCenter(0, -70));
        scale = 1.66f;
        add(new GuiShip(TextureRegister.shipEngiSmall0, (int) (69 * scale), (int) (42 * scale)).atCenter(309, -70));

        Font font = Engine.getFontManager().getFont(FontType.XOLONIUM.getFontName());
        Label label = new Label(font, languageManager.getString("gui.selectFaction.maintext"), 24);
        add(label.atCenter(0, 108));

        int discFontSize = 16;
        add(new Label(font, languageManager.getString("gui.selectFaction.humanDisc"), discFontSize).setMaxWidth(300).atCenter(-248, 64));
        add(new Label(font, languageManager.getString("gui.selectFaction.saimonDisc"), discFontSize).setMaxWidth(300).atCenter(69, 64));
        add(new Label(font, languageManager.getString("gui.selectFaction.engiDisc"), discFontSize).setMaxWidth(300).atCenter(378, 64));
    }

    private static class GuiShip extends TexturedRotatedRectangle {
        GuiShip(TextureRegister textureRegister, int width, int height) {
            super(textureRegister, width, height);
        }

        @Override
        public void update(int mouseX, int mouseY) {
            super.update(mouseX, mouseY);

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