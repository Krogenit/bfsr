package net.bfsr.client.gui.ingame;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.font.StringObject;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.component.armor.Armor;
import net.bfsr.component.armor.ArmorPlate;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.Shield;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.client.PacketShipControl;
import org.joml.Vector2f;

public class ShipHUD {
    private final TexturedGuiObject armorPlate = new TexturedGuiObject(TextureRegister.guiArmorPlate);
    private final TexturedGuiObject energy = new TexturedGuiObject(TextureRegister.guiEnergy);
    private final TexturedGuiObject hudShip = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipSecondary = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipAdd0 = new TexturedGuiObject(TextureRegister.guiHudShipAdd);
    private final TexturedGuiObject shield = new TexturedGuiObject(TextureRegister.guiShield);

    private final StringObject shipCargo = new StringObject(FontType.CONSOLA);
    private final StringObject shipCrew = new StringObject(FontType.CONSOLA);
    private final StringObject textHull = new StringObject(FontType.CONSOLA);
    private final StringObject textShield = new StringObject(FontType.CONSOLA);

    private final StringObject controlText = new StringObject(FontType.XOLONIUM, Lang.getString("gui.control"), 16);
    private Ship currentShip;
    @Getter
    private Ship otherShip;

    private final Vector2f rotationVector = new Vector2f();
    private final AbstractTexture shieldTexture = Engine.assetsManager.textureLoader.getTexture(TextureRegister.shieldSmall0);
    private final Core core = Core.get();
    private final PlayerInputController playerInputController = core.getInputHandler().getPlayerInputController();
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractGUIRenderer guiRenderer = renderer.guiRenderer;
    private final RenderManager renderManager = core.getWorldRenderer().getRenderManager();
    private final GuiManager guiManager = core.getGuiManager();

    public void init(GuiInGame gui) {
        int scaleX = 280;
        int scaleY = 220;
        hudShip.setSize(scaleX, scaleY);
        hudShip.atBottomRightCorner(-scaleX, -scaleY);
        gui.registerGuiObject(hudShip);

        hudShipSecondary.setSize(scaleX, scaleY);
        hudShipSecondary.atTopRightCorner(-scaleX, 0);
        gui.registerGuiObject(hudShipSecondary);

        scaleX = 140;
        scaleY = 72;
        hudShipAdd0.setSize(scaleX, scaleY);
        hudShipAdd0.atBottomRightCorner(-hudShip.getWidth() - scaleX + 20, -scaleY);
        gui.registerGuiObject(hudShipAdd0);

        Button buttonControl = new Button(TextureRegister.guiButtonControl, () -> {
            Ship playerControlledShip = playerInputController.getShip();
            if (playerControlledShip != null) {
                core.sendTCPPacket(new PacketShipControl(playerControlledShip.getId(), false));
                playerInputController.setShip(null);
                playerInputController.disableShipDeselection();
                selectShip(playerControlledShip);
                onShipControlCanceled();
            } else if (currentShip != null && canControlShip(currentShip)) {
                playerInputController.setShip(currentShip);
                core.sendTCPPacket(new PacketShipControl(currentShip.getId(), true));
            }
        }) {
            @Override
            public void updateMouseHover() {
                if (guiManager.getCurrentGui() == null) {
                    super.updateMouseHover();
                }
            }
        };
        buttonControl.setSize(256, 40);
        buttonControl.atBottomRightCorner(-128 - hudShip.getWidth() / 2, -hudShip.getHeight() - 26);

        if (playerInputController.isControllingShip()) {
            controlText.setString(Lang.getString("gui.cancelControl"));
        } else {
            controlText.setString(Lang.getString("gui.control"));
        }
        gui.registerGuiObject(controlText.atBottomRightCorner(-hudShip.getWidth() / 2 - controlText.getWidth() / 2, -hudShip.getHeight() - 1));
        gui.registerGuiObject(buttonControl);

        shipCargo.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 26);
        shipCrew.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 40);
    }

    public void update() {
        if (currentShip != null && currentShip.isDead()) {
            currentShip = null;
        }

        if (otherShip != null && otherShip.isDead()) {
            otherShip = null;
        }
    }

    private void renderShield(Shield shield, int x, int y) {
        float shieldValue = shield.getShield() / shield.getMaxShield();
        int shieldSize = (int) (220 * shield.getSize());
        renderQuad(x, y, shieldSize, shieldSize, 1.0f - shieldValue, shieldValue, 0.0f, 1.0f, this.shield.getTexture());
    }

    private void renderShipInHUD(Ship ship, int x, int y, float shipSize) {
        float hull = ship.getHull().getHull() / ship.getHull().getMaxHull();
        AbstractTexture texture = renderManager.getRender(ship.getId()).getTexture();
        renderQuad(x, y, -MathUtils.HALF_PI, (int) (ship.getSize().x * shipSize), (int) (ship.getSize().y * shipSize), 1.0f - hull, hull, 0.0f, 1.0f, texture);
    }

    private void renderHullValue(Ship ship, int x, int y) {
        textHull.setString(String.valueOf(Math.round(ship.getHull().getHull())));
        textHull.setPosition(x - textHull.getWidth() / 2, y + 16);
        guiRenderer.addCentered(x, y + 12, textHull.getWidth() + 8, 18, 0.0f, 0.0f, 0.0f, 1.0f, shieldTexture);
        textHull.renderNoInterpolation();
    }

    private void renderShieldValue(Shield shield, int x, int y) {
        textShield.setString(String.valueOf(Math.round(shield.getShield())));
        textShield.setPosition(x - textShield.getWidth() / 2, y + 74);
        guiRenderer.addCentered(x, y + 70, textShield.getWidth() + 8, 18, 0.0f, 0.0f, 0.0f, 1.0f, shieldTexture);
        textShield.renderNoInterpolation();
    }

    private void renderArmorPlates(Ship ship, int x, int y) {
        Armor armor = ship.getArmor();
        ArmorPlate[] plates = armor.getArmorPlates();
        float rot = MathUtils.PI;
        for (int i = 0; i < 4; i++) {
            ArmorPlate plate = plates[i];
            rot -= MathUtils.HALF_PI;
            if (plate != null) {
                RotationHelper.rotate(rot, -56, 0, rotationVector);
                rotationVector.x += x;
                rotationVector.y += y;
                float armorPlateValue = plate.getArmor() / plate.getArmorMax();
                renderQuad((int) rotationVector.x, (int) rotationVector.y, rot + MathUtils.PI, 64, 64, 1.0f - armorPlateValue, armorPlateValue, 0.0f, 1.0f, armorPlate.getTexture());
            }
        }
    }

    private void renderWeaponSlots(Ship ship, int x, int y, float shipSize) {
        int size = ship.getWeaponSlots().size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = ship.getWeaponSlots().get(i);
            if (slot != null) {
                float reload = slot.getReloadTimer() / (float) slot.getTimeToReload();
                Vector2f pos = slot.getLocalPosition();
                RotationHelper.rotate(-MathUtils.HALF_PI, pos.x, pos.y, rotationVector);
                int slotWidth = (int) (slot.getSize().x * shipSize);
                int slothHeight = (int) (slot.getSize().y * shipSize);
                ShipRender render = renderManager.getRender(ship.getId());
                AbstractTexture texture = render.getWeaponSlotTexture(i);
                renderQuad((int) (x + rotationVector.x * shipSize), (int) (y + rotationVector.y * shipSize), -MathUtils.HALF_PI, slotWidth, slothHeight,
                        reload, 0.0f, 1.0f - reload, 1.0f, texture);
            }
        }
    }

    private void renderCurrentShipInfo() {
        int x = hudShip.getX() + hudShip.getWidth() / 2;
        int y = hudShip.getY() + hudShip.getHeight() / 2;
        float shipSize = 10.0f;

        renderShipInHUD(currentShip, x, y, shipSize);
        renderHullValue(currentShip, x, y);

        Shield shield = currentShip.getShield();
        if (shield != null && shield.isShieldAlive()) {
            renderShield(shield, x, y);
            renderShieldValue(shield, x, y);
        }

        renderArmorPlates(currentShip, x, y);

        AbstractTexture energyText = energy.getTexture();
        int energyWidth = 16;
        int energyHeight = 8;
        Reactor reactor = currentShip.getReactor();
        float energy = reactor.getEnergy() / reactor.getMaxEnergy() * 20.0f;
        for (int i = 0; i < 20; i++) {
            float rot = (float) (i * 0.08f - Math.PI / 4.0f);
            RotationHelper.rotate(rot, -100, 0, rotationVector);
            rotationVector.x += x;
            rotationVector.y += y;
            renderQuad((int) rotationVector.x, (int) rotationVector.y, -MathUtils.PI + rot, energyWidth, energyHeight, 0.0f, 0.0f, 0.0f, 1.0f, energyText);
            if (energy >= i) {
                renderQuad((int) rotationVector.x, (int) rotationVector.y, (float) (-Math.PI + rot), energyWidth, energyHeight, 0.25f, 0.5f, 1.0f, 1.0f, energyText);
            }
        }

        shipCargo.setString(Lang.getString(Lang.getString("gui.shipCargo") + ": " + currentShip.getCargo().getCapacity() + "/" + currentShip.getCargo().getMaxCapacity()));
        shipCargo.renderNoInterpolation();

        shipCrew.setString(Lang.getString(Lang.getString("gui.shipCrew") + ": " + currentShip.getCrew().getCrewSize() + "/" + currentShip.getCrew().getMaxCrewSize()));
        shipCrew.renderNoInterpolation();

        renderWeaponSlots(currentShip, x, y, shipSize);
    }

    private void renderOtherShipInfo() {
        int x = hudShipSecondary.getX() + hudShipSecondary.getWidth() / 2;
        int y = hudShipSecondary.getY() + hudShipSecondary.getHeight() / 2;

        float shipSize = 10.0f;

        renderShipInHUD(otherShip, x, y, shipSize);

        Shield shield = otherShip.getShield();
        if (shield != null && shield.isShieldAlive()) {
            renderShield(shield, x, y);
        }

        renderArmorPlates(otherShip, x, y);
        renderWeaponSlots(otherShip, x, y, shipSize);
    }

    public void selectShip(Ship ship) {
        currentShip = ship;
    }

    public void selectShipSecondary(Ship ship) {
        if (ship == null) {
            if (otherShip != null) {
                otherShip = null;
            }
        } else {
            otherShip = ship;
        }
    }

    private boolean canControlShip(Ship s) {
        return core.getPlayerName().equals(s.getName());
    }

    public void onShipControlStarted() {
        controlText.setString(Lang.getString("gui.cancelControl"));
    }

    private void onShipControlCanceled() {
        controlText.setString(Lang.getString("gui.control"));
    }

    public void render() {
        if (currentShip != null) renderCurrentShipInfo();
        if (otherShip != null) renderOtherShipInfo();
    }

    private void renderQuad(int x, int y, float rot, int width, int height, float r, float g, float b, float a, AbstractTexture texture) {
        guiRenderer.add(x, y, rot, width, height, r, g, b, a, texture);
    }

    private void renderQuad(int x, int y, int width, int height, float r, float g, float b, float a, AbstractTexture texture) {
        guiRenderer.addCentered(x, y, width, height, r, g, b, a, texture);
    }

    public void resize() {
        shipCargo.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 26);
        shipCrew.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 40);
    }
}