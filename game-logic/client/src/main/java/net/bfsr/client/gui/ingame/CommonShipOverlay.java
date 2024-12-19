package net.bfsr.client.gui.ingame;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.gui.component.TexturedRotatedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: use renderer instead of overriding render method
 */
public abstract class CommonShipOverlay extends TexturedRectangle {
    protected final Client client = Client.get();
    private final RenderManager renderManager = client.getRenderManager();
    private final Vector2f rotationVector = new Vector2f();

    private final Label textShield = new Label(Font.CONSOLA_FT);
    private final TexturedRotatedRectangle shipGuiObject = new TexturedRotatedRectangle(AbstractTextureLoader.dummyTexture, 100, 100);

    private final List<Rectangle> hullCells = new ArrayList<>();
    private final List<Rectangle> armorCells = new ArrayList<>();
    private final List<TexturedRotatedRectangle> weaponSlotGuiObjects = new ArrayList<>();
    private final List<TexturedRotatedRectangle> energyValues = new ArrayList<>();

    private final TexturedRectangle shieldGuiObject = new TexturedRectangle(TextureRegister.guiShield, 210, 210);
    private final TexturedRectangle shieldValueGuiObject = new TexturedRectangle(TextureRegister.shieldSmall0, textShield.getWidth() + 8,
            18);
    private float lastShieldValue;

    @Setter
    @Getter
    @Nullable
    protected Ship ship;

    CommonShipOverlay() {
        super(TextureRegister.guiHudShip, 280, 220);
        add(shieldGuiObject.atCenter(0, 0));
        shieldValueGuiObject.add(textShield.atBottom(0, 0));
    }

    void addShip() {
        remove(shipGuiObject);
        int width = (int) (ship.getSizeX() / shipGuiObject.getTexture().getWidth() * 60);
        int height = (int) (ship.getSizeY() / shipGuiObject.getTexture().getHeight() * 60);
        add(shipGuiObject.setSize(width, height).atCenter(0, 0).setAllColors(0.1f, 0.1f, 0.1f, 0.6f).setRotation(MathUtils.HALF_PI));
        shipGuiObject.setTexture(client.getRenderManager().getRender(ship.getId()).getTexture());
    }

    void addHullCells() {
        if (hullCells.size() > 0) {
            for (int i = 0; i < hullCells.size(); i++) {
                remove(hullCells.get(i));
            }

            hullCells.clear();
        }

        int cellSize = 28;
        int offset = 1;
        Hull hull = ship.getModules().getHull();
        HullCell[][] cells = hull.getCells();
        int startX = cells[0].length * cellSize / 2 + (cells[0].length - 1) * offset / 2 - cellSize / 2;
        int y1 = -cells.length * cellSize / 2 - (cells.length - 1) * offset / 2 + cellSize / 2;
        float x1;

        for (int i = 0, size = cells.length; i < size; i++) {
            x1 = startX;
            for (int j = 0, size1 = cells[0].length; j < size1; j++) {
                HullCell cell = cells[i][j];
                if (cell != null) {
                    float armorPlateValue = cell.getValue() / cell.getMaxValue();
                    Rectangle cellRectangle = new Rectangle(cellSize, cellSize);
                    add(cellRectangle.atCenter((int) x1, y1).setColor(1.0f - armorPlateValue,
                            armorPlateValue, 0.0f, 0.25f));
                    hullCells.add(cellRectangle);
                    x1 -= cellSize + offset;
                }
            }

            y1 += cellSize + offset;
        }
    }

    void addArmorPlates() {
        if (armorCells.size() > 0) {
            for (int i = 0; i < armorCells.size(); i++) {
                remove(armorCells.get(i));
            }

            armorCells.clear();
        }

        float cellSize = 28 / 1.75f;
        float offset = 13;
        Armor armor = ship.getModules().getArmor();
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
        remove(shieldValueGuiObject);
        add(shieldValueGuiObject.atCenter(0, -72).setWidthFunction((width, height) -> textShield.getWidth() + 8)
                .setAllColors(0.0f, 0.0f, 0.0f, 1.0f));
    }

    void addWeaponSlots() {
        if (weaponSlotGuiObjects.size() > 0) {
            for (int i = 0; i < weaponSlotGuiObjects.size(); i++) {
                remove(weaponSlotGuiObjects.get(i));
            }

            weaponSlotGuiObjects.clear();
        }

        float shipSize = 14.5f;
        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            float reload = slot.getReloadTimer() / (float) slot.getTimeToReload();
            Vector2f pos = slot.getLocalPosition();
            RotationHelper.rotate(MathUtils.HALF_PI, pos.x, pos.y, rotationVector);
            int slotWidth = (int) (slot.getSizeX() * shipSize);
            int slothHeight = (int) (slot.getSizeY() * shipSize);
            Render render = renderManager.getRender(ship.getId());
            if (render instanceof ShipRender shipRender) {
                AbstractTexture texture = shipRender.getWeaponSlotTexture(i);

                TexturedRotatedRectangle texturedRectangle = new TexturedRotatedRectangle(texture, slotWidth, slothHeight);
                add(texturedRectangle.atCenter(Math.round(rotationVector.x * shipSize), Math.round(rotationVector.y * shipSize))
                        .setColor(reload, 0.0f, 1.0f - reload, 0.75f).setRotation(MathUtils.HALF_PI));
                weaponSlotGuiObjects.add(texturedRectangle);
            }
        }
    }

    void addEnergy() {
        if (energyValues.size() > 0) {
            for (int i = 0; i < energyValues.size(); i++) {
                remove(energyValues.get(i));
            }

            energyValues.clear();
        }

        int energyWidth = 16;
        int energyHeight = 8;
        Reactor reactor = ship.getModules().getReactor();
        float energy = reactor.getEnergy() / reactor.getMaxEnergy() * 20.0f;
        for (int i = 0; i < 20; i++) {
            float rot = (float) (i * -0.08f + Math.PI / 4.0f);
            RotationHelper.rotate(rot, -100, 0, rotationVector);
            TexturedRotatedRectangle rectangle = new TexturedRotatedRectangle(TextureRegister.guiEnergy, energyWidth, energyHeight);
            add(rectangle.atCenter((int) rotationVector.x, (int) rotationVector.y).setRotation(-MathUtils.PI + rot));
            energyValues.add(rectangle);
            if (energy >= i) {
                rectangle.setColor(0.25f, 0.5f, 1.0f, 1.0f);
            } else {
                rectangle.setColor(0.0f, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    @Override
    public void addToScene() {
        super.addToScene();
        rebuildScene();
    }

    @Override
    public void update() {
        super.update();

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
            float shieldValue = shield.getShieldHp() / shield.getShieldMaxHp();
            shieldGuiObject.setAllColors(1.0f - shieldValue, shieldValue, 0.0f, 1.0f);

            if (lastShieldValue != shieldValue) {
                textShield.setString(String.valueOf(Math.round(shield.getShieldHp())));
                lastShieldValue = shieldValue;
            }
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
        if (energyValues.isEmpty()) {
            return;
        }

        Reactor reactor = ship.getModules().getReactor();
        float energy = reactor.getEnergy() / reactor.getMaxEnergy() * 20.0f;
        for (int i = 0; i < 20; i++) {
            TexturedRotatedRectangle rectangle = energyValues.get(i);

            if (energy >= i) {
                rectangle.setColor(0.25f, 0.5f, 1.0f, 1.0f);
            } else {
                rectangle.setColor(0.0f, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    protected abstract void rebuildScene();

    protected void onCurrentShipSelected() {
        if (isOnScene) {
            rebuildScene();
        }
    }

    protected void onCurrentShipDeselected() {}

    public void selectShip(Ship ship) {
        if (this.ship != null) {
            onCurrentShipDeselected();
        }

        this.ship = ship;

        if (this.ship != null) {
            onCurrentShipSelected();
        }
    }
}
