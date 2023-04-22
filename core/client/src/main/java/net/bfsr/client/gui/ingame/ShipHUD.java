package net.bfsr.client.gui.ingame;

import lombok.Getter;
import net.bfsr.client.component.Shield;
import net.bfsr.client.component.weapon.WeaponSlot;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.language.Lang;
import net.bfsr.client.network.packet.client.PacketShipControl;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.texture.TextureRegister;
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
    private final Texture shieldTexture = TextureLoader.getTexture(TextureRegister.shieldSmall0);

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
            WorldClient w = Core.get().getWorld();
            Ship playerControlledShip = w.getPlayerShip();
            if (playerControlledShip != null) {
                Core.get().sendTCPPacket(new PacketShipControl(playerControlledShip.getId(), false));
                Core.get().getWorld().setPlayerShip(null);
                Core.get().getWorld().disableShipDeselection();
                selectShip(playerControlledShip);
                onShipControlCanceled();
            } else if (currentShip != null && canControlShip(currentShip)) {
                Core.get().getWorld().setPlayerShip(currentShip);
                Core.get().sendTCPPacket(new PacketShipControl(currentShip.getId(), true));
            }
        }) {
            @Override
            public void updateMouseHover() {
                if (Core.get().getCurrentGui() == null) {
                    super.updateMouseHover();
                }
            }
        };
        buttonControl.setSize(256, 40);
        buttonControl.atBottomRightCorner(-128 - hudShip.getWidth() / 2, -hudShip.getHeight() - 26);

        if (Core.get().getWorld() != null && Core.get().getWorld().getPlayerShip() == null) {
            controlText.setString(Lang.getString("gui.control"));
        } else {
            controlText.setString(Lang.getString("gui.cancelControl"));
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
        renderQuad(x, y, -MathUtils.HALF_PI, (int) (ship.getScale().x * shipSize), (int) (ship.getScale().y * shipSize), 1.0f - hull, hull, 0.0f, 1.0f, ship.getTexture());
    }

    private void renderHullValue(Ship ship, int x, int y) {
        textHull.setString(Math.round(ship.getHull().getHull()) + "");
        textHull.setPosition(x - textHull.getWidth() / 2, y + 16);
        GUIRenderer.get().addCentered(x, y + 12, textHull.getWidth() + 8, 18, 0.0f, 0.0f, 0.0f, 1.0f, shieldTexture);
        textHull.renderNoInterpolation();
    }

    private void renderShieldValue(ShieldCommon shield, int x, int y) {
        textShield.setString(Math.round(shield.getShield()) + "");
        textShield.setPosition(x - textShield.getWidth() / 2, y + 74);
        GUIRenderer.get().addCentered(x, y + 70, textShield.getWidth() + 8, 18, 0.0f, 0.0f, 0.0f, 1.0f, shieldTexture);
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
                float reload = slot.getReloadTimer() / slot.getTimeToReload();
                Vector2f pos = slot.getLocalPosition();
                RotationHelper.rotate((float) (-Math.PI / 2.0f), pos.x, pos.y, rotationVector);
                int slotWidth = (int) (slot.getScale().x * shipSize);
                int slothHeight = (int) (slot.getScale().y * shipSize);
                renderQuad((int) (x + rotationVector.x * shipSize), (int) (y + rotationVector.y * shipSize), -MathUtils.HALF_PI, slotWidth, slothHeight,
                        reload, 0.0f, 1.0f - reload, 1.0f, slot.getTexture());
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

        Texture energyText = energy.getTexture();
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
        return Core.get().getPlayerName().equals(s.getName());
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

    private void renderQuad(int x, int y, float rot, int width, int height, float r, float g, float b, float a, Texture texture) {
        GUIRenderer.get().add(x, y, rot, width, height, r, g, b, a, texture);
    }

    private void renderQuad(int x, int y, int width, int height, float r, float g, float b, float a, Texture texture) {
        GUIRenderer.get().addCentered(x, y, width, height, r, g, b, a, texture);
    }

    public void resize() {
        shipCargo.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 26);
        shipCrew.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 40);
    }
}