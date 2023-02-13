package net.bfsr.client.gui;

import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.packet.client.PacketFactionSelect;

public class GuiFactionSelect extends Gui {
    private final TextureObject shipHuman = new TextureObject(TextureRegister.shipHumanSmall0);
    private final TextureObject shipSaimon = new TextureObject(TextureRegister.shipSaimonSmall0);
    private final TextureObject shipEngi = new TextureObject(TextureRegister.shipEngiSmall0);
    private AxisAlignedBoundingBox aabbHuman, aabbSaimon, aabbEngi;

    @Override
    protected void initElements() {
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiFactionSelect).atCenter(-1024 / 2, -600 / 2).setSize(1024, 600));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiLogoBFSR).atCenter(-216 / 2, -200 - 216 / 2).setSize(216, 216));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiBfsrText2).atCenter(-860 / 2, -200 - 80 / 2).setSize(860, 80));
        registerGuiObject(new Button(Lang.getString("gui.selectFaction.human"), () -> {
            Core.get().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.HUMAN));
            Core.get().setCurrentGui(null);
        }).atCenter(-309 - 300 / 2, 230 - 50 / 2));
        registerGuiObject(new Button(Lang.getString("gui.selectFaction.saimon"), () -> {
            Core.get().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.SAIMON));
            Core.get().setCurrentGui(null);
        }).atCenter(-1 - 300 / 2, 230 - 50 / 2));
        registerGuiObject(new Button(Lang.getString("gui.selectFaction.engi"), () -> {
            Core.get().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.ENGI));
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

        aabbHuman = new AxisAlignedBoundingBox(center.x - 460, center.y - 88, center.x - 160, center.y + 260);
        aabbSaimon = new AxisAlignedBoundingBox(center.x - 150, center.y - 88, center.x + 150, center.y + 260);
        aabbEngi = new AxisAlignedBoundingBox(center.x + 160, center.y - 88, center.x + 460, center.y + 260);

        StringObject stringObject = new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.maintext"), 24).compile();
        registerGuiObject(stringObject.atCenter(-stringObject.getStringWidth() / 2, -96));

        int discFontSize = 16;
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.humanDisc"), discFontSize).compile().atCenter(-450, -64));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.saimonDisc"), discFontSize).compile().atCenter(-142, -64));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.selectFaction.engiDisc"), discFontSize).compile().atCenter(166, -64));
    }

    private void updateRot(TextureObject ship, AxisAlignedBoundingBox aabb) {
        float rotSpeed = 0.04f;

        ship.setLastRotation(ship.getRotation());
        if (aabb.isIntersects(Mouse.getPosition())) {
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
        shipHuman.render();
        shipSaimon.render();
        shipEngi.render();
    }
}
