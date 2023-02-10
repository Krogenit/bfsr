package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.input.InputChat;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.InstancedRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.Shield;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.client.PacketShipControl;
import net.bfsr.settings.EnumOption;
import net.bfsr.world.World;
import net.bfsr.world.WorldClient;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiInGame extends Gui {
    private final Core core = Core.get();

    private final TexturedGuiObject armorPlate = new TexturedGuiObject(TextureRegister.guiArmorPlate);
    private final TexturedGuiObject energy = new TexturedGuiObject(TextureRegister.guiEnergy);
    private final TexturedGuiObject hudShip = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipSecondary = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipAdd0 = new TexturedGuiObject(TextureRegister.guiHudShipAdd);
    private final TexturedGuiObject map = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject shield = new TexturedGuiObject(TextureRegister.guiShield);
    private final TexturedGuiObject chat = new TexturedGuiObject(TextureRegister.guiChat);

    private final StringObject controlText = new StringObject(FontType.XOLONIUM, Lang.getString("gui.control"), 16);
    private final StringObject shipCargo = new StringObject(FontType.CONSOLA);
    private final StringObject shipCrew = new StringObject(FontType.CONSOLA);
    private final StringObject textHull = new StringObject(FontType.CONSOLA);
    private final StringObject textShield = new StringObject(FontType.CONSOLA);

    private final InputChat chatInput = new InputChat();

    private Ship currentShip;
    private Ship otherShip;

    private final AxisAlignedBoundingBox shipAABB = new AxisAlignedBoundingBox();
    private final AxisAlignedBoundingBox mapBoundingBox = new AxisAlignedBoundingBox();
    private final Vector4f color = new Vector4f();
    private final Vector2f rotationVector = new Vector2f();
    private final Texture shieldTexture = TextureLoader.getTexture(TextureRegister.shieldSmall0);
    private final DebugInfoElement debugInfoElement = new DebugInfoElement();

    @Override
    protected void initElements() {
        int scaleX = 280;
        int scaleY = 220;
        hudShip.setSize(scaleX, scaleY);
        hudShip.atBottomRightCorner(-scaleX, -scaleY);
        registerGuiObject(hudShip);

        hudShipSecondary.setSize(scaleX, scaleY);
        hudShipSecondary.atTopRightCorner(-scaleX, 0);
        registerGuiObject(hudShipSecondary);

        map.setSize(scaleX, scaleY).atUpperLeftCorner(0, 0);
        registerGuiObject(map);

        scaleX = 140;
        scaleY = 72;
        hudShipAdd0.setSize(scaleX, scaleY);
        hudShipAdd0.atBottomRightCorner(-hudShip.getWidth() - scaleX + 20, -scaleY);
        registerGuiObject(hudShipAdd0);

        scaleX = 320;
        scaleY = 170;
        chat.setSize(scaleX, scaleY);
        chat.atBottomLeftCorner(0, -scaleY);
        registerGuiObject(chat);

        int chatWidth = 320;
        int chatHeight = 170;
        chatInput.setSize(chatWidth, chatHeight);
        chatInput.atBottomLeftCorner(0, -chatHeight);
        registerGuiObject(chatInput);

        Button buttonControl = new Button(TextureRegister.guiButtonControl, () -> {
            WorldClient w = core.getWorld();
            Ship playerControlledShip = w.getPlayerShip();
            if (playerControlledShip != null) {
                core.sendPacket(new PacketShipControl(playerControlledShip.getId(), false));
                core.getWorld().setPlayerShip(null);
                core.getWorld().disableShipDeselection();
                selectShip(playerControlledShip);
                cancelShipControl();
            } else if (currentShip != null && canControlShip(currentShip)) {
                core.getWorld().setPlayerShip(currentShip);
                core.sendPacket(new PacketShipControl(currentShip.getId(), true));
            }
        });
        buttonControl.setSize(256, 40);
        buttonControl.atBottomRightCorner(-128 - hudShip.getWidth() / 2, -hudShip.getHeight() - 26);

        if (core.getWorld() != null && core.getWorld().getPlayerShip() == null) {
            controlText.update(Lang.getString("gui.control"));
        } else {
            controlText.update(Lang.getString("gui.cancelControl"));
        }
        registerGuiObject(controlText.atBottomRightCorner(-hudShip.getWidth() / 2 - controlText.getStringWidth() / 2, -hudShip.getHeight() - 1));
        registerGuiObject(buttonControl);
    }

    public void addChatMessage(String message) {
        chatInput.addNewLineToChat(message);
    }

    @Override
    public void onMouseLeftClicked() {
        super.onMouseLeftClicked();
        chatInput.onMouseLeftClick();
    }

    public boolean isActive() {
        return chatInput.isActive();
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ESCAPE && Core.get().canControlShip()) {
            Core.get().setCurrentGui(new GuiInGameMenu());
        }
    }

    @Override
    public void onMouseLeftRelease() {
        super.onMouseLeftRelease();
        chatInput.onMouseLeftRelease();
    }

    @Override
    public void onMouseScroll(float y) {
        super.onMouseScroll(y);
        chatInput.scroll(y);
    }

    @Override
    public void update() {
        if (EnumOption.IS_DEBUG.getBoolean()) debugInfoElement.update(6, map.getHeight() + 6);

        if (currentShip != null && currentShip.isDead()) {
            currentShip = null;
        }

        if (otherShip != null && otherShip.isDead()) {
            otherShip = null;
        }

        super.update();
    }

    private boolean canControlShip(Ship s) {
        return core.getPlayerName().equals(s.getName());
    }

    public void setShipControl() {
        controlText.update(Lang.getString("gui.cancelControl"));
    }

    private void cancelShipControl() {
        controlText.update(Lang.getString("gui.control"));
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

    public void selectShip(Ship ship) {
        currentShip = ship;
    }

    private void renderMap(World world) {
        List<Ship> ships = world.getShips();
        Vector2f camPos = core.getRenderer().getCamera().getPosition();
        float mapOffsetX = 600;
        float mapOffsetY = 600;
        mapBoundingBox.set(camPos.x - mapOffsetX, camPos.y - mapOffsetY, camPos.x + mapOffsetX, camPos.y + mapOffsetY);
        float mapScaleX = 5.0f;
        float mapScaleY = 7.0f;
        float shipSize = 1.0f;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int offsetY = 17;
        int offsetX = 22;
        GL11.glScissor(map.getX() + offsetX, height - map.getHeight() + offsetY, map.getWidth() - (offsetX << 1), map.getHeight() - (offsetY << 1));

        int miniMapX = map.getX() + map.getWidth() / 2;
        int miniMapY = map.getY() + map.getHeight() / 2;
        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            Vector2f pos = s.getPosition();
            Vector2f scale = s.getScale();
            float sX = scale.x * shipSize / 2.0f;
            float sY = scale.y * shipSize / 2.0f;
            shipAABB.set(pos.x - sX, pos.y - sY, pos.x + sX, pos.y + sY);
            if (mapBoundingBox.isIntersects(shipAABB)) {
                Faction faction = s.getFaction();
                if (faction == Faction.ENGI) {
                    color.x = 0.5f;
                    color.y = 1.0f;
                    color.z = 0.5f;
                } else if (faction == Faction.HUMAN) {
                    color.x = 0.5f;
                    color.y = 0.5f;
                    color.z = 1.0f;
                } else {
                    color.x = 1.0f;
                    color.y = 1.0f;
                    color.z = 0.5f;
                }

                int x = (int) (miniMapX + (pos.x - camPos.x) / mapScaleX);
                int y = (int) (miniMapY + (pos.y - camPos.y) / mapScaleY);
                int sizeX = (int) (scale.x * shipSize);
                int sizeY = (int) (scale.y * shipSize);
                InstancedRenderer.INSTANCE.addToRenderPipeLineSinCos(x, y, x, y, s.getLastSin(), s.getLastCos(), s.getSin(), s.getCos(), sizeX, sizeY, color.x, color.y, color.z, 1.0f,
                        s.getTexture(), BufferType.GUI);
            }
        }

        InstancedRenderer.INSTANCE.render(BufferType.GUI);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void renderShipInHUD(Ship ship, int x, int y, float shipSize) {
        float hull = ship.getHull().getHull() / ship.getHull().getMaxHull();
        renderQuad(1.0f - hull, hull, 0.0f, 1.0f, ship.getTexture(), x, y, (float) (-Math.PI / 2.0f), (int) (ship.getScale().x * shipSize), (int) (ship.getScale().y * shipSize));
    }

    private void renderHullValue(Ship ship, int x, int y) {
        textHull.update(Math.round(ship.getHull().getHull()) + "");
        textHull.setPosition(x - textHull.getStringWidth() / 2, y + 16);
        renderQuad(0.0f, 0.0f, 0.0f, 1.0f, shieldTexture, x, y + 12, 0, textHull.getStringWidth() + 8, 18);
        textHull.render();
    }

    private void renderShield(Shield shield, int x, int y) {
        float shieldValue = shield.getShield() / shield.getMaxShield();
        int shieldSize = (int) (220 * shield.getSize());
        renderQuad(1.0f - shieldValue, shieldValue, 0.0f, 1.0f, this.shield.getTexture(), x, y, 0, shieldSize, shieldSize);
    }

    private void renderShieldValue(Shield shield, int x, int y) {
        textShield.update(Math.round(shield.getShield()) + "");
        textShield.setPosition(x - textShield.getStringWidth() / 2, y + 74);
        renderQuad(0.0f, 0.0f, 0.0f, 1.0f, shieldTexture, x, y + 70, 0, textShield.getStringWidth() + 8, 18);
        textShield.render();
    }

    private void renderArmorPlates(Ship ship, int x, int y) {
        Armor armor = ship.getArmor();
        ArmorPlate[] plates = armor.getArmorPlates();
        float rot = (float) Math.PI;
        for (int i = 0; i < 4; i++) {
            ArmorPlate plate = plates[i];
            rot -= Math.PI / 2.0;
            if (plate != null) {
                RotationHelper.rotate(rot, -56, 0, rotationVector);
                rotationVector.x += x;
                rotationVector.y += y;
                float armorPlateValue = plate.getArmor() / plate.getArmorMax();
                renderQuad(1.0f - armorPlateValue, armorPlateValue, 0.0f, 1.0f, armorPlate.getTexture(), (int) rotationVector.x, (int) rotationVector.y, (float) (rot + Math.PI), 64, 64);
            }
        }
    }

    private void renderWeaponSlots(Ship ship, int x, int y, float shipSize) {
        int size = ship.getWeaponSlots().size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = ship.getWeaponSlots().get(i);
            if (slot != null) {
                float reload = slot.getShootTimer() / slot.getShootTimerMax();
                Vector2f pos = slot.getAddPosition();
                RotationHelper.rotate((float) (-Math.PI / 2.0f), pos.x, pos.y, rotationVector);
                int slotWidth = (int) (slot.getScale().x * shipSize);
                int slothHeight = (int) (slot.getScale().y * shipSize);
                renderQuad(reload, 0.0f, 1.0f - reload, 1.0f, slot.getTexture(), (int) (x + rotationVector.x * shipSize), (int) (y + rotationVector.y * shipSize),
                        (float) (-Math.PI / 2.0f), slotWidth, slothHeight);
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
        if (shield != null && shield.shieldAlive()) {
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
            renderQuad(0.0f, 0.0f, 0.0f, 1.0f, energyText, (int) rotationVector.x, (int) rotationVector.y, (float) (-Math.PI + rot), energyWidth, energyHeight);
            if (energy >= i) {
                renderQuad(0.25f, 0.5f, 1.0f, 1.0f, energyText, (int) rotationVector.x, (int) rotationVector.y, (float) (-Math.PI + rot), energyWidth, energyHeight);
            }
        }

        shipCargo.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 26);
        shipCargo.update(Lang.getString(Lang.getString("gui.shipCargo") + ": " + currentShip.getCargo().getCapacity() + "/" + currentShip.getCargo().getMaxCapacity()));
        shipCargo.render();

        shipCrew.setPosition(hudShipAdd0.getX() + 16, hudShipAdd0.getY() + 40);
        shipCrew.update(Lang.getString(Lang.getString("gui.shipCrew") + ": " + currentShip.getCrew().getCrewSize() + "/" + currentShip.getCrew().getMaxCrewSize()));
        shipCrew.render();

        renderWeaponSlots(currentShip, x, y, shipSize);
    }

    private void renderOtherShipInfo() {
        int x = hudShipSecondary.getX() + hudShipSecondary.getWidth() / 2;
        int y = hudShipSecondary.getY() + hudShipSecondary.getHeight() / 2;

        float shipSize = 10.0f;

        renderShipInHUD(otherShip, x, y, shipSize);

        Shield shield = otherShip.getShield();
        if (shield != null && shield.shieldAlive()) {
            renderShield(shield, x, y);
        }

        renderArmorPlates(otherShip, x, y);
        renderWeaponSlots(otherShip, x, y, shipSize);
    }

    @Override
    public void render(float interpolation) {
        super.render(interpolation);
        InstancedRenderer.INSTANCE.render(BufferType.GUI);
        renderMap(Core.get().getWorld());
        if (currentShip != null) renderCurrentShipInfo();
        if (otherShip != null) renderOtherShipInfo();

        if (EnumOption.IS_DEBUG.getBoolean()) debugInfoElement.render();
    }

    private void renderQuad(float r, float g, float b, float a, Texture texture, int x, int y, float rot, int width, int height) {
        InstancedRenderer.INSTANCE.addToRenderPipeLine(x, y, rot, width, height, r, g, b, a, texture, BufferType.GUI);
    }

    public void setPing(long ping) {
        debugInfoElement.setPing(ping);
    }
}
