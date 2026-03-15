package net.bfsr.client.gui.ingame;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.assets.TextureRegister;
import net.bfsr.client.gui.GuiStyle;
import net.bfsr.client.renderer.EntityRenderer;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.gui.component.TexturedRotatedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.ModuleWithCells;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CommonShipOverlay extends Rectangle {
    protected final Client client = Client.get();
    private final EntityRenderer entityRenderer = client.getEntityRenderer();
    private final Vector2f rotationVector = new Vector2f();

    private final Label textShield = new Label(Engine.getFontManager().getDefaultFont());
    private final TexturedRotatedRectangle shipGuiObject = new TexturedRotatedRectangle(Engine.getRenderer().getDummyTexture(), 100, 100);

    private final List<Rectangle> hullCells = new ArrayList<>();
    private final List<Rectangle> armorCells = new ArrayList<>();
    private final List<TexturedRotatedRectangle> weaponSlotGuiObjects = new ArrayList<>();
    private final Rectangle reactorValueRectangle = new Rectangle(10, 100);

    private final TexturedRectangle shieldGuiObject = new TexturedRectangle(TextureRegister.guiShield.getTextureData(), 190, 190);
    private final Rectangle shieldValueBackground = new Rectangle(textShield.getWidth() + 8, 18);
    private final float fixedShipScale = 145.0f;
    private final float fixedCellSize = 150.0f;
    private final float fixedOffsetSize = 7.5f;

    private float lastShieldValue;
    private float dynamicShipScale = 1.0f;

    @Getter
    @Nullable
    protected Ship ship;

    CommonShipOverlay() {
        super(280, 220);
        add(shieldGuiObject.atCenter(0, 0));
        shieldValueBackground.add(textShield.atBottom(0, 0));
        GuiStyle.setupTransparentRectangle(this);

        Rectangle rectangle = new Rectangle(10, 100);
        GuiStyle.setupTransparentRectangle(rectangle);
        add(rectangle.atLeft(10, 0));
        Vector3f uiColor = GuiStyle.UI_COLOR;
        rectangle.add(reactorValueRectangle.setAllColors(uiColor.x, uiColor.y, uiColor.z, 0.9f).atBottom(0, 0));
    }

    void addShip() {
        remove(shipGuiObject);
        AbstractTexture texture = client.getEntityRenderer().getRender(ship.getId()).getTexture();
        int width = Math.round(dynamicShipScale * ship.getSizeX() * fixedShipScale);
        int height = Math.round(dynamicShipScale * ship.getSizeY() * fixedShipScale);

        add(shipGuiObject.setSize(width, height).atCenter(0, 0).setAllColors(0.1f, 0.1f, 0.1f, 0.6f)
                .setRotation(MathUtils.HALF_PI));
        shipGuiObject.setTexture(texture);
    }

    void addHullCells() {
        if (hullCells.size() > 0) {
            for (int i = 0; i < hullCells.size(); i++) {
                remove(hullCells.get(i));
            }

            hullCells.clear();
        }

        Hull hull = ship.getModules().getHull();
        float cellSizeScale = calculateHullCellSize(hull);
        float cellSize = fixedCellSize * cellSizeScale;
        float offset = fixedOffsetSize * cellSizeScale;

        HullCell[][] cells = hull.getCells();
        float startX = cells[0].length * cellSize / 2.0f + (cells[0].length - 1) * offset / 2 - cellSize / 2.0f;
        float y1 = -cells.length * cellSize / 2.0f - (cells.length - 1) * offset / 2 + cellSize / 2.0f;
        float x1;

        for (int i = 0, size = cells.length; i < size; i++) {
            x1 = startX;
            for (int j = 0, size1 = cells[0].length; j < size1; j++) {
                HullCell cell = cells[i][j];
                if (cell != null) {
                    float armorPlateValue = cell.getValue() / cell.getMaxValue();
                    int cellSizeInt = Math.round(cellSize);
                    Rectangle cellRectangle = new Rectangle(cellSizeInt, cellSizeInt);
                    add(cellRectangle.atCenter(Math.round(x1), Math.round(y1)).setColor(1.0f - armorPlateValue,
                            armorPlateValue, 0.0f, 0.25f));
                    hullCells.add(cellRectangle);
                    x1 -= cellSize + offset;
                }
            }

            y1 += cellSize + offset;
        }
    }

    private float calculateHullCellSize(ModuleWithCells<?> module) {
        float shipWidth = ship.getSizeX();
        float shipHeight = ship.getSizeY();
        float cellSizeScale;
        if (shipWidth > shipHeight) {
            cellSizeScale = shipWidth / module.getCells().length * dynamicShipScale;
        } else {
            cellSizeScale = shipHeight / module.getCells()[0].length * dynamicShipScale;
        }

        return cellSizeScale;
    }

    void addArmorPlates() {
        if (armorCells.size() > 0) {
            for (int i = 0; i < armorCells.size(); i++) {
                remove(armorCells.get(i));
            }

            armorCells.clear();
        }

        Armor armor = ship.getModules().getArmor();
        float cellSizeScale = calculateHullCellSize(armor);
        float cellSize = fixedCellSize * 0.5f * cellSizeScale * dynamicShipScale;
        float offset = fixedOffsetSize * 10.0f * cellSizeScale * dynamicShipScale;

        ArmorPlate[][] cells = armor.getCells();
        float startX = cells[0].length * cellSize * 0.5f + (cells[0].length - 1) * offset * 0.5f - cellSize / 2;
        float y1 = -cells.length * cellSize * 0.5f - (cells.length - 1) * offset * 0.5f + cellSize / 2;
        float x1;

        for (int i = 0, size = cells.length; i < size; i++) {
            x1 = startX;
            for (int j = 0, size1 = cells[0].length; j < size1; j++) {
                ArmorPlate plate = cells[i][j];
                if (plate != null) {
                    float armorPlateValue = plate.getValue() / plate.getMaxValue();
                    int cellSizeInt = Math.round(cellSize);
                    Rectangle cellRectangle = new Rectangle(cellSizeInt, cellSizeInt);
                    add(cellRectangle.atCenter(Math.round(x1), Math.round(y1)).setColor(1.0f - armorPlateValue,
                            armorPlateValue, 0.0f, 0.25f));
                    armorCells.add(cellRectangle);
                    x1 -= cellSize + offset;
                }
            }

            y1 += cellSize + offset;
        }
    }

    void addShieldValue() {
        remove(shieldValueBackground);
        add(shieldValueBackground.atCenter(0, -72).setWidthFunction((width, height) -> textShield.getWidth() + 8)
                .setAllColors(0.0f, 0.0f, 0.0f, 0.8f));
    }

    void addWeaponSlots() {
        if (weaponSlotGuiObjects.size() > 0) {
            for (int i = 0; i < weaponSlotGuiObjects.size(); i++) {
                remove(weaponSlotGuiObjects.get(i));
            }

            weaponSlotGuiObjects.clear();
        }

        float shipScale = fixedShipScale * dynamicShipScale;
        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            float reload = slot.getReloadTimer() / (float) slot.getTimeToReload();
            Vector2f pos = slot.getLocalPosition();
            RotationHelper.rotate(MathUtils.HALF_PI, pos.x, pos.y, rotationVector);
            int slotWidth = (int) (slot.getSizeX() * shipScale);
            int slothHeight = (int) (slot.getSizeY() * shipScale);
            Render render = entityRenderer.getRender(ship.getId());
            if (render instanceof ShipRender shipRender) {
                AbstractTexture texture = shipRender.getWeaponSlotTexture(i);

                TexturedRotatedRectangle texturedRectangle = new TexturedRotatedRectangle(texture, slotWidth, slothHeight);
                add(texturedRectangle.atCenter(Math.round(rotationVector.x * shipScale), Math.round(rotationVector.y * shipScale))
                        .setColor(reload, 0.0f, 1.0f - reload, 0.75f).setRotation(MathUtils.HALF_PI));
                weaponSlotGuiObjects.add(texturedRectangle);
            }
        }
    }

    @Override
    public void addToScene() {
        super.addToScene();
        rebuildScene();
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        if (ship != null) {
            if (ship.isDead()) {
                ship = null;
            } else {
                updateShield();
                updateHull();
                updateArmor();
                updateWeaponSlots();
                updateReactor();
            }
        }
    }

    private void updateShield() {
        Shield shield = ship.getModules().getShield();
        if (shield != null && shield.isAlive()) {
            addIfAbsent(shieldGuiObject);
            addIfAbsent(shieldValueBackground);

            float shieldValue = shield.getShieldHp() / shield.getShieldMaxHp();
            shieldGuiObject.setAllColors(1.0f - shieldValue, shieldValue, 0.0f, 1.0f);

            if (lastShieldValue != shieldValue) {
                textShield.setString(String.valueOf(Math.round(shield.getShieldHp())));
                lastShieldValue = shieldValue;
            }
        } else {
            remove(shieldGuiObject);
            remove(shieldValueBackground);
        }
    }

    private void updateHull() {
        Hull hull = ship.getModules().getHull();
        HullCell[][] cells = hull.getCells();
        for (int i = 0, size = cells.length; i < size; i++) {
            for (int j = 0, size1 = cells[0].length; j < size1; j++) {
                HullCell cell = cells[i][j];
                Rectangle rectangle = hullCells.get(i * size1 + j);
                if (cell != null) {
                    float armorPlateValue = cell.getValue() / cell.getMaxValue();
                    if (armorPlateValue <= 0.0f) {
                        rectangle.setColor(0.0f, 0.0f, 0.0f, 0.25f);
                    } else {
                        rectangle.setColor(1.0f - armorPlateValue, armorPlateValue, 0.0f, 0.25f);
                    }
                }
            }
        }
    }

    private void updateArmor() {
        Armor armor = ship.getModules().getArmor();
        if (armor != null) {
            HullCell[][] cells = armor.getCells();
            for (int i = 0, size = cells.length; i < size; i++) {
                for (int j = 0, size1 = cells[0].length; j < size1; j++) {
                    HullCell cell = cells[i][j];
                    Rectangle rectangle = armorCells.get(i * size1 + j);

                    if (cell != null) {
                        float armorPlateValue = cell.getValue() / cell.getMaxValue();
                        if (armorPlateValue <= 0.0f) {
                            rectangle.setColor(0.0f, 0.0f, 0.0f, 0.25f);
                        } else {
                            rectangle.setColor(1.0f - armorPlateValue, armorPlateValue, 0.0f, 0.25f);
                        }
                    }
                }
            }
        }
    }

    private void updateWeaponSlots() {
        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            float reload = slot.getReloadTimer() / (float) slot.getTimeToReload();
            weaponSlotGuiObjects.get(i).setColor(reload, 0.0f, 1.0f - reload, 0.75f);
        }
    }

    private void updateReactor() {
        Reactor reactor = ship.getModules().getReactor();
        float energy = reactor.getEnergy() / reactor.getMaxEnergy();
        reactorValueRectangle.setSize(10, Math.round(100 * energy));
    }

    protected abstract void rebuildScene();

    private void calculateScale() {
        dynamicShipScale = 1.0f;

        /*
         * We use height instead of width because ship is rotated in GUI
         */
        int maxWidth = height - 70;
        int maxHeight = width - 70;

        float width = ship.getSizeX() * fixedShipScale;
        float height = ship.getSizeY() * fixedShipScale;

        if (width > maxWidth) {
            dynamicShipScale = maxWidth / width;
        }

        if (height * dynamicShipScale > maxHeight) {
            dynamicShipScale = maxHeight / (height * dynamicShipScale);
        }
    }

    protected void onShipAdd() {
        calculateScale();

        if (isOnScene) {
            rebuildScene();
        }
    }

    protected void onShipRemove() {}

    public void setShip(Ship ship) {
        if (this.ship != null) {
            onShipRemove();
        }

        this.ship = ship;

        if (this.ship != null) {
            onShipAdd();
        }
    }
}
