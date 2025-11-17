package net.bfsr.client.gui.faction;

import net.bfsr.client.Client;
import net.bfsr.client.assets.TextureRegister;
import net.bfsr.client.gui.GuiStyle;
import net.bfsr.client.gui.objects.SimpleButton;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.gui.component.TexturedRotatedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.texture.TextureData;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.client.PacketFactionSelect;

public class GuiFactionSelect extends Gui {
    public GuiFactionSelect() {
        Client client = Client.get();
        LanguageManager languageManager = client.getLanguageManager();
        GuiObject background = new GuiObject(930, 560);
        Rectangle top = new Rectangle(930, 208);
        background.add(GuiStyle.setupTransparentRectangle(top).atTopLeft(0, 0));
        Rectangle left = new Rectangle(310, 352);
        background.add(GuiStyle.setupTransparentRectangle(left).atTopLeft(0, -208));
        Rectangle center = new Rectangle(310, 352);
        background.add(GuiStyle.setupTransparentRectangle(center).atTopLeft(310, -208));
        Rectangle right = new Rectangle(310, 352);
        background.add(GuiStyle.setupTransparentRectangle(right).atTopLeft(620, -208));
        add(background.atCenter(0, 0));

        top.add(new TexturedRectangle(TextureRegister.guiLogoBFSR.getTextureData(), 216, 216).atTop(0, 20));
        left.add(new SimpleButton(languageManager.getString("gui.selectFaction.human"), (mouseX, mouseY) -> {
            client.sendTCPPacket(new PacketFactionSelect(Faction.HUMAN));
            client.closeGui();
        }).atBottom(0, 5));
        center.add(new SimpleButton(languageManager.getString("gui.selectFaction.saimon"), (mouseX, mouseY) -> {
            client.sendTCPPacket(new PacketFactionSelect(Faction.SAIMON));
            client.closeGui();
        }).atBottom(0, 5));
        right.add(new SimpleButton(languageManager.getString("gui.selectFaction.engi"), (mouseX, mouseY) -> {
            client.sendTCPPacket(new PacketFactionSelect(Faction.ENGI));
            client.closeGui();
        }).atBottom(0, 5));

        ShipRegistry shipRegistry = client.getConfigConverterManager().getConverter(ShipRegistry.class);
        left.add(new GuiShip(shipRegistry.get("human_small0").getTextureData(), 103, 118).atCenter(0, 0));
        float scale = 1.33f;
        center.add(new GuiShip(shipRegistry.get("saimon_small0").getTextureData(), (int) (100 * scale), (int) (65 * scale))
                .atCenter(0, 0));
        scale = 1.66f;
        right.add(new GuiShip(shipRegistry.get("engi_small0").getTextureData(), (int) (69 * scale), (int) (42 * scale))
                .atCenter(0, 0));

        Font font = Engine.getFontManager().getDefaultFont();
        Label label = new Label(font, languageManager.getString("gui.selectFaction.maintext"), 24);
        top.add(label.atBottom(0, 10));

        int discFontSize = 16;
        left.add(new Label(font, languageManager.getString("gui.selectFaction.humanDisc"), discFontSize).setMaxWidth(300)
                .atTopLeft(6, -6));
        center.add(new Label(font, languageManager.getString("gui.selectFaction.saimonDisc"), discFontSize).setMaxWidth(300)
                .atTopLeft(6, -6));
        right.add(new Label(font, languageManager.getString("gui.selectFaction.engiDisc"), discFontSize).setMaxWidth(300)
                .atTopLeft(6, -6));
    }

    private static class GuiShip extends TexturedRotatedRectangle {
        GuiShip(TextureData textureData, int width, int height) {
            super(textureData, width, height);
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