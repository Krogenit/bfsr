package net.bfsr.client.gui.ingame;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.client.PacketShipControl;
import org.joml.Vector2f;

import java.util.List;

public class ShipHUD {
    private final TexturedGuiObject energy = new TexturedGuiObject(TextureRegister.guiEnergy);
    private final TexturedGuiObject hudShip = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipSecondary = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipAdd0 = new TexturedGuiObject(TextureRegister.guiHudShipAdd);
    private final TexturedGuiObject shield = new TexturedGuiObject(TextureRegister.guiShield);

    private final StringObject shipCargo = new StringObject(FontType.CONSOLA);
    private final StringObject shipCrew = new StringObject(FontType.CONSOLA);
    private final StringObject textShield = new StringObject(FontType.CONSOLA);

    private final StringObject controlText = new StringObject(FontType.XOLONIUM, Lang.getString("gui.control"), 16);
    private Ship currentShip;
    @Getter
    private Ship otherShip;

    private final Vector2f rotationVector = new Vector2f();
    private final AbstractTexture shieldTexture = Engine.assetsManager.getTexture(TextureRegister.shieldSmall0);
    private final Core core = Core.get();
    private final PlayerInputController playerInputController = core.getInputHandler().getPlayerInputController();
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractGUIRenderer guiRenderer = renderer.guiRenderer;
    private final RenderManager renderManager = core.getRenderManager();
    private final GuiManager guiManager = core.getGuiManager();

    private HUD hud;

    public void init(HUD hud) {
        this.hud = hud;
        int scaleX = 280;
        int scaleY = 220;
        hudShip.setSize(scaleX, scaleY);
        hudShip.atBottomRightCorner(-scaleX, -scaleY);
        hud.registerGuiObject(hudShip);

        hudShipSecondary.setSize(scaleX, scaleY);
        hudShipSecondary.atTopRightCorner(-scaleX, 0);
        hud.registerGuiObject(hudShipSecondary);

        scaleX = 140;
        scaleY = 72;
        hudShipAdd0.setSize(scaleX, scaleY);
        hudShipAdd0.atBottomRightCorner(-hudShip.getWidth() - scaleX + 20, -scaleY);
        hud.registerGuiObject(hudShipAdd0);

        Button buttonControl = new Button(TextureRegister.guiButtonControl, () -> {
            Ship playerControlledShip = playerInputController.getShip();
            if (playerControlledShip != null) {
                core.sendTCPPacket(new PacketShipControl(playerControlledShip.getId(), false));
                playerInputController.resetControlledShip();
                selectShip(playerControlledShip);
                onShipControlCanceled();
            } else if (currentShip != null) {
                playerInputController.setShip(currentShip);
                core.sendTCPPacket(new PacketShipControl(currentShip.getId(), true));
            }
        }) {
            @Override
            public void updateMouseHover() {
                if (guiManager.noGui()) {
                    super.updateMouseHover();
                }
            }
        };
        buttonControl.setSize(256, 40);
        buttonControl.atBottomRightCorner(-128 - hudShip.getWidth() / 2, -hudShip.getHeight() - 26);

        if (playerInputController.isControllingShip()) {
            controlText.setStringAndCompileAtOrigin(Lang.getString("gui.cancelControl"));
        } else {
            controlText.setStringAndCompileAtOrigin(Lang.getString("gui.control"));
        }
        hud.registerGuiObject(
                controlText.atBottomRightCorner(-hudShip.getWidth() / 2 - controlText.getWidth() / 2, -hudShip.getHeight() - 1));
        hud.registerGuiObject(buttonControl);
    }

    public void update() {
        if (currentShip != null && currentShip.isDead()) {
            currentShip = null;
            onCurrentShipDeselected();
        }

        if (otherShip != null && otherShip.isDead()) {
            otherShip = null;
        }
    }

    private void onCurrentShipDeselected() {
        hud.unregisterGuiObject(shipCargo);
        hud.unregisterGuiObject(shipCrew);
    }

    private void onCurrentShipSelected() {
        Cargo cargo = currentShip.getModules().getCargo();
        shipCargo.setStringAndCompileAtOrigin(Lang.getString(Lang.getString("gui.shipCargo") + ": " + cargo.getCapacity() + "/" +
                cargo.getMaxCapacity()));
        hud.registerGuiObject(shipCargo.atBottomRightCorner(-384, -46));

        Crew crew = currentShip.getModules().getCrew();
        shipCrew.setStringAndCompileAtOrigin(Lang.getString(Lang.getString("gui.shipCrew") + ": " + crew.getCrewSize() + "/" +
                crew.getMaxCrewSize()));
        hud.registerGuiObject(shipCrew.atBottomRightCorner(-384, -32));
    }

    private void renderShield(Shield shield, int x, int y) {
        float shieldValue = shield.getShieldHp() / shield.getShieldMaxHp();
        int shieldSize = (int) (220 * shield.getSize().x);
        renderQuad(x, y, shieldSize, shieldSize, 1.0f - shieldValue, shieldValue, 0.0f, 1.0f, this.shield.getTexture());
    }

    private void renderHullValue(Ship ship, int x, int y) {
        float scale = 20.0f;
        float cellSize = scale;
        float offset = 1;
        Hull hull = ship.getModules().getHull();
        HullCell[][] cells = hull.getCells();
        float startX = -cells[0].length * cellSize * 0.5f - (cells[0].length - 1) * offset * 0.5f;
        float y1 = cells.length * cellSize * 0.5f + (cells.length - 1) * offset * 0.5f - cellSize;
        float x1;

        for (int i = 0; i < cells.length; i++) {
            x1 = startX;
            for (int j = 0; j < cells[0].length; j++) {
                HullCell cell = cells[i][j];
                if (cell != null) {
                    float armorPlateValue = cell.getValue() / cell.getMaxValue();
                    guiRenderer.add(x + x1, y + y1, cellSize, cellSize, 1.0f - armorPlateValue,
                            armorPlateValue, 0.0f, 0.25f);
                    x1 += cellSize + offset;
                }
            }

            y1 -= cellSize + offset;
        }
    }

    private void renderShieldValue(Shield shield, int x, int y) {
        textShield.setPosition(x - textShield.getWidth() / 2, y + 74);
        textShield.setStringAndCompile(String.valueOf(Math.round(shield.getShieldHp())));
        guiRenderer.addCentered(x, y + 70, textShield.getWidth() + 8, 18, 0.0f, 0.0f, 0.0f, 1.0f, shieldTexture);
        textShield.renderNoInterpolation();
    }

    private void renderArmorPlates(Ship ship, int x, int y, float scale) {
        float cellSize = scale / 1.75f;
        float offset = 9.5f;
        Armor armor = ship.getModules().getArmor();
        ArmorPlate[][] cells = armor.getCells();
        float startX = -cells[0].length * cellSize * 0.5f - (cells[0].length - 1) * offset * 0.5f;
        float y1 = cells.length * cellSize * 0.5f + (cells.length - 1) * offset * 0.5f - cellSize;
        float x1;

        for (int i = 0; i < cells.length; i++) {
            x1 = startX;
            for (int j = 0; j < cells[0].length; j++) {
                ArmorPlate plate = cells[i][j];
                if (plate != null) {
                    float armorPlateValue = plate.getValue() / plate.getMaxValue();
                    guiRenderer.add(x + x1, y + y1, cellSize, cellSize, 1.0f - armorPlateValue,
                            armorPlateValue, 0.0f, 0.25f);
                    x1 += cellSize + offset;
                }
            }

            y1 -= cellSize + offset;
        }
    }

    private void renderWeaponSlots(Ship ship, int x, int y, float shipSize) {
        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            if (slot != null) {
                float reload = slot.getReloadTimer() / (float) slot.getTimeToReload();
                Vector2f pos = slot.getLocalPosition();
                RotationHelper.rotate(-MathUtils.HALF_PI, pos.x, pos.y, rotationVector);
                int slotWidth = (int) (slot.getSize().x * shipSize);
                int slothHeight = (int) (slot.getSize().y * shipSize);
                Render<?> render = renderManager.getRender(ship.getId());
                if (render instanceof ShipRender shipRender) {
                    AbstractTexture texture = shipRender.getWeaponSlotTexture(i);
                    renderQuad((int) (x + rotationVector.x * shipSize), (int) (y + rotationVector.y * shipSize),
                            -MathUtils.HALF_PI, slotWidth, slothHeight, reload, 0.0f, 1.0f - reload, 1.0f, texture);
                }
            }
        }
    }

    private void renderCurrentShipInfo() {
        int x = hudShip.getX() + hudShip.getWidth() / 2;
        int y = hudShip.getY() + hudShip.getHeight() / 2;
        float shipSize = 10.0f;

        renderHullValue(currentShip, x, y);

        Shield shield = currentShip.getModules().getShield();
        if (shield != null && shield.isAlive()) {
            renderShield(shield, x, y);
            renderShieldValue(shield, x, y);
        }

        renderArmorPlates(currentShip, x, y, shipSize * 2.0f);

        AbstractTexture energyText = energy.getTexture();
        int energyWidth = 16;
        int energyHeight = 8;
        Reactor reactor = currentShip.getModules().getReactor();
        float energy = reactor.getEnergy() / reactor.getMaxEnergy() * 20.0f;
        for (int i = 0; i < 20; i++) {
            float rot = (float) (i * 0.08f - Math.PI / 4.0f);
            RotationHelper.rotate(rot, -100, 0, rotationVector);
            rotationVector.x += x;
            rotationVector.y += y;
            renderQuad((int) rotationVector.x, (int) rotationVector.y, -MathUtils.PI + rot, energyWidth, energyHeight, 0.0f, 0.0f,
                    0.0f, 1.0f, energyText);
            if (energy >= i) {
                renderQuad((int) rotationVector.x, (int) rotationVector.y, (float) (-Math.PI + rot), energyWidth, energyHeight,
                        0.25f, 0.5f, 1.0f, 1.0f, energyText);
            }
        }

        renderWeaponSlots(currentShip, x, y, shipSize);
    }

    private void renderOtherShipInfo() {
        int x = hudShipSecondary.getX() + hudShipSecondary.getWidth() / 2;
        int y = hudShipSecondary.getY() + hudShipSecondary.getHeight() / 2;

        float shipSize = 10.0f;

        Shield shield = otherShip.getModules().getShield();
        if (shield != null && shield.isAlive()) {
            renderShield(shield, x, y);
        }

        renderArmorPlates(otherShip, x, y, shipSize * 2.0f);
        renderWeaponSlots(otherShip, x, y, shipSize);
    }

    public void selectShip(Ship ship) {
        if (currentShip != null) {
            onCurrentShipDeselected();
        }

        currentShip = ship;

        if (currentShip != null) {
            onCurrentShipSelected();
        }
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
        controlText.setStringAndCompileAtOrigin(Lang.getString("gui.cancelControl"));
    }

    private void onShipControlCanceled() {
        controlText.setStringAndCompileAtOrigin(Lang.getString("gui.control"));
    }

    public void render() {
        if (currentShip != null) renderCurrentShipInfo();
        if (otherShip != null) renderOtherShipInfo();
    }

    private void renderQuad(int x, int y, float rot, int width, int height, float r, float g, float b, float a,
                            AbstractTexture texture) {
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