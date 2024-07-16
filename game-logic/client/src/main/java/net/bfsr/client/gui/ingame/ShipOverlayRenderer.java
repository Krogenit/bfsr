package net.bfsr.client.gui.ingame;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.armor.ArmorPlate;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import java.util.List;

/**
 * TODO: use renderer instead of overriding render method
 */
public abstract class ShipOverlayRenderer extends TexturedRectangle {
    private final AbstractGUIRenderer guiRenderer = Engine.renderer.guiRenderer;
    final AbstractTexture energy = Engine.assetsManager.getTexture(TextureRegister.guiEnergy);
    private final AbstractTexture shield = Engine.assetsManager.getTexture(TextureRegister.guiShield);
    private final AbstractTexture shieldTexture = Engine.assetsManager.getTexture(TextureRegister.shieldSmall0);
    private final Label textShield = new Label(Font.CONSOLA);
    private final Core core = Core.get();
    private final RenderManager renderManager = core.getRenderManager();
    final Vector2f rotationVector = new Vector2f();

    @Setter
    @Getter
    @Nullable
    protected Ship ship;

    ShipOverlayRenderer() {
        super(TextureRegister.guiHudShip, 280, 220);
    }

    @Override
    public void update() {
        super.update();

        if (ship != null && ship.isDead()) {
            ship = null;
        }
    }

    @Override
    public void render(AbstractGUIRenderer guiRenderer, int lastX, int lastY, int x, int y) {
        super.render(guiRenderer, lastX, lastY, x, y);
        if (ship != null) renderShipInfo();
    }

    protected abstract void renderShipInfo();

    void renderShield(Shield shield, int x, int y) {
        float shieldValue = shield.getShieldHp() / shield.getShieldMaxHp();
        int shieldSize = (int) (220 * shield.getSize().x);
        renderQuad(x, y, shieldSize, shieldSize, 1.0f - shieldValue, shieldValue, 0.0f, 1.0f, this.shield);
    }

    void renderHullValue(Ship ship, int x, int y) {
        float scale = 20.0f;
        float cellSize = scale;
        float offset = 1;
        Hull hull = ship.getModules().getHull();
        HullCell[][] cells = hull.getCells();
        float startX = -cells[0].length * cellSize * 0.5f - (cells[0].length - 1) * offset * 0.5f;
        float y1 = cells.length * cellSize * 0.5f + (cells.length - 1) * offset * 0.5f - cellSize;
        float x1;

        for (int i = 0, size = cells.length; i < size; i++) {
            x1 = startX;
            for (int j = 0, size1 = cells[0].length; j < size1; j++) {
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

    /**
     * TODO: optimize shield value rendering
     */
    void renderShieldValue(Shield shield, int x, int y) {
        textShield.setString(String.valueOf(Math.round(shield.getShieldHp())));
        guiRenderer.addCentered(x, y + 70, textShield.getWidth() + 8, 18, 0.0f, 0.0f, 0.0f, 1.0f, shieldTexture);
        int x1 = x - textShield.getWidth() / 2;
        int y1 = y + 64;
        textShield.render(guiRenderer, x1, y1, x1, y1);
    }

    void renderArmorPlates(Ship ship, int x, int y, float scale) {
        float cellSize = scale / 1.75f;
        float offset = 9.5f;
        Armor armor = ship.getModules().getArmor();
        ArmorPlate[][] cells = armor.getCells();
        float startX = -cells[0].length * cellSize * 0.5f - (cells[0].length - 1) * offset * 0.5f;
        float y1 = cells.length * cellSize * 0.5f + (cells.length - 1) * offset * 0.5f - cellSize;
        float x1;

        for (int i = 0, size = cells.length; i < size; i++) {
            x1 = startX;
            for (int j = 0, size1 = cells[0].length; j < size1; j++) {
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

    void renderWeaponSlots(Ship ship, int x, int y, float shipSize) {
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
                Render render = renderManager.getRender(ship.getId());
                if (render instanceof ShipRender shipRender) {
                    AbstractTexture texture = shipRender.getWeaponSlotTexture(i);
                    renderQuad((int) (x + rotationVector.x * shipSize), (int) (y + rotationVector.y * shipSize),
                            -MathUtils.HALF_PI, slotWidth, slothHeight, reload, 0.0f, 1.0f - reload, 1.0f, texture);
                }
            }
        }
    }

    void renderQuad(int x, int y, float rot, int width, int height, float r, float g, float b, float a, AbstractTexture texture) {
        guiRenderer.addRotated(x, y, rot, width, height, r, g, b, a, texture);
    }

    private void renderQuad(int x, int y, int width, int height, float r, float g, float b, float a, AbstractTexture texture) {
        guiRenderer.addCentered(x, y, width, height, r, g, b, a, texture);
    }
}
