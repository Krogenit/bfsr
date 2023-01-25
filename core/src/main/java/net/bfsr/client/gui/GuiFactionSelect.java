package net.bfsr.client.gui;

import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.string.StaticString;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.faction.Faction;
import net.bfsr.math.RotationHelper;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.client.PacketFactionSelect;
import org.joml.Vector2f;

public class GuiFactionSelect extends Gui {
    private final TextureObject fon;
    private final TextureObject logo;
    private final TextureObject logoText;

    private final TextureObject shipHuman;
    private final TextureObject shipSaimon;
    private final TextureObject shipEngi;
    private AxisAlignedBoundingBox aabbHuman, aabbSaimon, aabbEngi;

    private StringObject mainText;
    private StringObject humanDisc, saimonDisc, engiDisc;

    public GuiFactionSelect() {
        fon = new GuiTextureObject(TextureRegister.guiFactionSelect);

        logo = new GuiTextureObject(TextureRegister.guiLogoBFSR);
        logoText = new GuiTextureObject(TextureRegister.guiBfsrText2);

        shipHuman = new GuiTextureObject(TextureRegister.shipHumanSmall0);
        shipSaimon = new GuiTextureObject(TextureRegister.shipSaimonSmall0);
        shipEngi = new GuiTextureObject(TextureRegister.shipEngiSmall0);
    }

    @Override
    protected void initElements() {
        Button b = new Button(center.x - 309, center.y + 230, "gui.selectFaction.human", () -> {
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.Human));
            Core.getCore().setCurrentGui(null);
        });
        registerGuiObject(b);
        b = new Button(center.x - 1, center.y + 230, "gui.selectFaction.saimon", () -> {
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.Saimon));
            Core.getCore().setCurrentGui(null);
        });
        registerGuiObject(b);
        b = new Button(center.x + 309, center.y + 230, "gui.selectFaction.engi", () -> {
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.Engi));
            Core.getCore().setCurrentGui(null);
        });
        registerGuiObject(b);

        fon.setPosition(new Vector2f(center.x, center.y));
        fon.setScale(1024, 600);

        logo.setPosition(new Vector2f(center.x, center.y - 200));
        logo.setScale(216, 216);

        logoText.setPosition(new Vector2f(center.x, center.y - 200));
        logoText.setScale(860, 80);

        shipHuman.setPosition(new Vector2f(center.x - 309, center.y + 70));
        shipHuman.setScale(90, 90);

        shipSaimon.setPosition(new Vector2f(center.x, center.y + 70));
        shipSaimon.setScale(110, 110);

        shipEngi.setPosition(new Vector2f(center.x + 309, center.y + 70));
        shipEngi.setScale(90, 90);

        aabbHuman = new AxisAlignedBoundingBox(Transformation.getOffsetByScale(new Vector2f(center.x - 460, center.y - 88)),
                Transformation.getOffsetByScale(new Vector2f(center.x - 160, center.y + 260)));
        aabbSaimon = new AxisAlignedBoundingBox(Transformation.getOffsetByScale(new Vector2f(center.x - 150, center.y - 88)),
                Transformation.getOffsetByScale(new Vector2f(center.x + 150, center.y + 260)));
        aabbEngi = new AxisAlignedBoundingBox(Transformation.getOffsetByScale(new Vector2f(center.x + 160, center.y - 88)),
                Transformation.getOffsetByScale(new Vector2f(center.x + 460, center.y + 260)));

        mainText = new StaticString(FontType.XOLONIUM, Lang.getString("gui.selectFaction.maintext"), center.x, center.y - 110, 16).compile();

        int discFontSize = 14;
        humanDisc = new StaticString(FontType.XOLONIUM, Lang.getString("gui.selectFaction.humanDisc"), center.x - 450, center.y - 80, discFontSize).compile();
        saimonDisc = new StaticString(FontType.XOLONIUM, Lang.getString("gui.selectFaction.saimonDisc"), center.x - 142, center.y - 80, discFontSize).compile();
        engiDisc = new StaticString(FontType.XOLONIUM, Lang.getString("gui.selectFaction.engiDisc"), center.x + 166, center.y - 80, discFontSize).compile();
    }

    private void updateRot(TextureObject ship, AxisAlignedBoundingBox aabb) {
        float rotSpeed = 0.04f;

        if (aabb.isIntersects(Mouse.getPosition())) {
            ship.setRotate(ship.getRotation() + rotSpeed);

            if (ship.getRotation() > RotationHelper.TWOPI) {
                ship.setRotate(ship.getRotation() - RotationHelper.TWOPI);
            }
        } else {
            if (ship.getRotation() > 0) {
                if (ship.getRotation() > Math.PI) {
                    float dif = RotationHelper.TWOPI - ship.getRotation();
                    if (dif < 0.01f) dif = 0.01f;
                    ship.setRotate(ship.getRotation() + rotSpeed * dif);

                    if (ship.getRotation() > RotationHelper.TWOPI) {
                        ship.setRotate(0);
                    }
                } else {
                    float dif = ship.getRotation() - 0;
                    if (dif < 0.01f) dif = 0.01f;
                    ship.setRotate(ship.getRotation() - rotSpeed * dif);

                    if (ship.getRotation() < 0) {
                        ship.setRotate(0);
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
    public void render(BaseShader shader) {
        fon.render(shader);
        logo.render(shader);
        logoText.render(shader);
        shipHuman.render(shader);
        shipSaimon.render(shader);
        shipEngi.render(shader);
        super.render(shader);
    }

    @Override
    public void clear() {
        if (mainText != null) mainText.clear();
        if (humanDisc != null) humanDisc.clear();
        if (saimonDisc != null) saimonDisc.clear();
        if (engiDisc != null) engiDisc.clear();
        super.clear();

    }
}
