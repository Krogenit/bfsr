package net.bfsr.client.gui;

import net.bfsr.client.font.FontRegistry;
import net.bfsr.client.font.GUIText;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.button.ButtonBase;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.faction.Faction;
import net.bfsr.math.RotationHelper;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.client.PacketFactionSelect;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GuiFactionSelect extends Gui {

    private final TextureObject fon;
    private final TextureObject logo;
    private final TextureObject logoText;

    private final TextureObject shipHuman;
    private final TextureObject shipSaimon;
    private final TextureObject shipEngi;
    private AxisAlignedBoundingBox aabbHuman, aabbSaimon, aabbEngi;

    private GUIText mainText;
    private GUIText humanDisc, saimonDisc, engiDisc;

    public GuiFactionSelect() {
        fon = new GuiTextureObject(TextureRegister.guiFactionSelect);

        logo = new GuiTextureObject(TextureRegister.guiLogoBFSR);
        logoText = new GuiTextureObject(TextureRegister.guiBfsrText2);

        shipHuman = new GuiTextureObject(TextureRegister.shipHumanSmall0);
        shipSaimon = new GuiTextureObject(TextureRegister.shipSaimonSmall0);
        shipEngi = new GuiTextureObject(TextureRegister.shipEngiSmall0);
    }

    @Override
    public void init() {
        super.init();

        Button b = new ButtonBase(0, new Vector2f(center.x - 309, center.y + 230), "gui.selectFaction.human");
        buttons.add(b);
        b = new ButtonBase(1, new Vector2f(center.x - 1, center.y + 230), "gui.selectFaction.saimon");
        buttons.add(b);
        b = new ButtonBase(2, new Vector2f(center.x + 309, center.y + 230), "gui.selectFaction.engi");
        buttons.add(b);

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

        mainText = new GUIText(Lang.getString("gui.selectFaction.maintext"), new Vector2f(1, 1),
                Transformation.getOffsetByScale(new Vector2f(center.x, center.y - 110)),
                new Vector4f(1, 1, 1, 1), true, EnumParticlePositionType.Gui);

        float discFontSize = 0.7f;
        humanDisc = new GUIText(Lang.getString("gui.selectFaction.humanDisc"), new Vector3f(discFontSize, discFontSize, 1.1f), FontRegistry.XOLONIUM,
                Transformation.getOffsetByScale(new Vector2f(center.x - 450, center.y - 80)), new Vector4f(1, 1, 1, 1),
                0.21f, false, EnumParticlePositionType.Gui);
        saimonDisc = new GUIText(Lang.getString("gui.selectFaction.saimonDisc"), new Vector3f(discFontSize, discFontSize, 1.1f), FontRegistry.XOLONIUM,
                Transformation.getOffsetByScale(new Vector2f(center.x - 142, center.y - 80)), new Vector4f(1, 1, 1, 1),
                0.21f, false, EnumParticlePositionType.Gui);
        engiDisc = new GUIText(Lang.getString("gui.selectFaction.engiDisc"), new Vector3f(discFontSize, discFontSize, 1.1f), FontRegistry.XOLONIUM,
                Transformation.getOffsetByScale(new Vector2f(center.x + 166, center.y - 80)), new Vector4f(1, 1, 1, 1),
                0.21f, false, EnumParticlePositionType.Gui);
    }

    @Override
    protected void onButtonLeftClick(Button b) {
        if (b.getId() == 0) {
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.Human));
            Core.getCore().setCurrentGui(null);
        } else if (b.getId() == 1) {
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.Saimon));
            Core.getCore().setCurrentGui(null);
        } else if (b.getId() == 2) {
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketFactionSelect(Faction.Engi));
            Core.getCore().setCurrentGui(null);
        }
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
