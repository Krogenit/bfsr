package net.bfsr.client.gui;

import net.bfsr.client.core.Core;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.network.packet.client.PacketFactionSelect;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.texture.TextureRegister;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;

public class GuiFactionSelect extends Gui {
    private final TextureObject shipHuman = new TextureObject();
    private final TextureObject shipSaimon = new TextureObject();
    private final TextureObject shipEngi = new TextureObject();
    private AABB aabbHuman, aabbSaimon, aabbEngi;
    private Texture textureShipHuman, textureShipSaimon, textureShipEngi;

    @Override
    protected void initElements() {
        textureShipHuman = TextureLoader.getTexture(TextureRegister.shipHumanSmall0);
        textureShipSaimon = TextureLoader.getTexture(TextureRegister.shipSaimonSmall0);
        textureShipEngi = TextureLoader.getTexture(TextureRegister.shipEngiSmall0);

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

        shipHuman.setPosition(center.x - 309, center.y + 70);
        shipHuman.getLastPosition().set(center.x - 309, center.y + 70);
        shipHuman.getLastScale().set(90);
        shipHuman.setScale(90, 90);

        shipSaimon.setPosition(center.x, center.y + 70);
        shipSaimon.getLastPosition().set(center.x, center.y + 70);
        shipSaimon.getLastScale().set(110);
        shipSaimon.setScale(110, 110);

        shipEngi.setPosition(center.x + 309, center.y + 70);
        shipEngi.getLastPosition().set(center.x + 309, center.y + 70);
        shipEngi.getLastScale().set(90);
        shipEngi.setScale(90, 90);

        aabbHuman = new AABB(center.x - 460, center.y - 88, center.x - 160, center.y + 260);
        aabbSaimon = new AABB(center.x - 150, center.y - 88, center.x + 150, center.y + 260);
        aabbEngi = new AABB(center.x + 160, center.y - 88, center.x + 460, center.y + 260);

        StringObject stringObject = new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.maintext"), 24).compile();
        registerGuiObject(stringObject.atCenter(-stringObject.getStringWidth() / 2, -96));

        int discFontSize = 16;
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.humanDisc"), discFontSize).compile().atCenter(-450, -64));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.saimonDisc"), discFontSize).compile().atCenter(-142, -64));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.engiDisc"), discFontSize).compile().atCenter(166, -64));
    }

    private void updateRot(TextureObject ship, AABB aabb) {
        float rotSpeed = 0.04f;

        ship.setLastRotation(ship.getRotation());
        Vector2f position = Mouse.getPosition();
        if (aabb.contains(position.x, position.y)) {
            ship.setRotation(ship.getRotation() + rotSpeed);

            if (ship.getRotation() > MathUtils.TWO_PI) {
                ship.setRotation(ship.getRotation() - MathUtils.TWO_PI);
            }
        } else {
            if (ship.getRotation() > 0) {
                if (ship.getRotation() > Math.PI) {
                    float dif = MathUtils.TWO_PI - ship.getRotation();
                    if (dif < 0.01f) dif = 0.01f;
                    ship.setRotation(ship.getRotation() + rotSpeed * dif);

                    if (ship.getRotation() > MathUtils.TWO_PI) {
                        ship.setRotation(0);
                    }
                } else {
                    float dif = ship.getRotation() - 0;
                    if (dif < 0.01f) dif = 0.01f;
                    ship.setRotation(ship.getRotation() - rotSpeed * dif);

                    if (ship.getRotation() < 0) {
                        ship.setRotation(0);
                    }
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        updateRot(shipHuman, aabbHuman);
        updateRot(shipSaimon, aabbSaimon);
        updateRot(shipEngi, aabbEngi);
    }

    @Override
    public void render(float interpolation) {
        super.render(interpolation);
        SpriteRenderer.INSTANCE.addToRenderPipeLine(shipHuman.getPosition().x, shipHuman.getPosition().y, shipHuman.getPosition().x, shipHuman.getPosition().y,
                shipHuman.getLastRotation(), shipHuman.getRotation(), shipHuman.getScale().x, shipHuman.getScale().y, shipHuman.getScale().x, shipHuman.getScale().y,
                1.0f, 1.0f, 1.0f, 1.0f, textureShipHuman, BufferType.GUI);
        SpriteRenderer.INSTANCE.addToRenderPipeLine(shipSaimon.getPosition().x, shipSaimon.getPosition().y, shipSaimon.getPosition().x, shipSaimon.getPosition().y,
                shipSaimon.getLastRotation(), shipSaimon.getRotation(), shipSaimon.getScale().x, shipSaimon.getScale().y, shipSaimon.getScale().x, shipSaimon.getScale().y,
                1.0f, 1.0f, 1.0f, 1.0f, textureShipSaimon, BufferType.GUI);
        SpriteRenderer.INSTANCE.addToRenderPipeLine(shipEngi.getPosition().x, shipEngi.getPosition().y, shipEngi.getPosition().x, shipEngi.getPosition().y,
                shipEngi.getLastRotation(), shipEngi.getRotation(), shipEngi.getScale().x, shipEngi.getScale().y, shipEngi.getScale().x, shipEngi.getScale().y,
                1.0f, 1.0f, 1.0f, 1.0f, textureShipEngi, BufferType.GUI);
    }
}