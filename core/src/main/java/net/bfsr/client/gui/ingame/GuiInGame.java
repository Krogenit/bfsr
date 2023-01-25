package net.bfsr.client.gui.ingame;

import lombok.Setter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.input.InputChat;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.string.DynamicString;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.Shield;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.math.RotationHelper;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.client.PacketShipControl;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.MainServer;
import net.bfsr.world.World;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuiInGame extends Gui {
    private final Core core = Core.getCore();

    private final DecimalFormat formatter = new DecimalFormat("0.00");

    private final TexturedGuiObject armorPlate = new TexturedGuiObject(TextureRegister.guiArmorPlate);
    private final TexturedGuiObject energy = new TexturedGuiObject(TextureRegister.guiEnergy);
    private final TexturedGuiObject hudShip = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipSecondary = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject hudShipAdd0 = new TexturedGuiObject(TextureRegister.guiHudShipAdd);
    private final TexturedGuiObject map = new TexturedGuiObject(TextureRegister.guiHudShip);
    private final TexturedGuiObject shield = new TexturedGuiObject(TextureRegister.guiShield);
    private final TexturedGuiObject chat = new TexturedGuiObject(TextureRegister.guiChat);

    private final float yOffset = 12.0f;
    @Setter
    private long ping;

    private final StringObject controlText = new DynamicString(FontType.XOLONIUM, Lang.getString("gui.control"), center.x + 490, center.y + 130, 16);
    private final StringObject upperText = new DynamicString(FontType.CONSOLA);
    private final StringObject worldText = new DynamicString(FontType.CONSOLA);
    private final StringObject shipText = new DynamicString(FontType.CONSOLA);
    private final StringObject shipCargo = new DynamicString(FontType.CONSOLA);
    private final StringObject shipCrew = new DynamicString(FontType.CONSOLA);
    private final StringObject textHull = new DynamicString(FontType.CONSOLA);
    private final StringObject textShield = new DynamicString(FontType.CONSOLA);

    private final InputChat chatInput = new InputChat();

    private boolean controlWasPressed;
    private boolean controlButtonCreated;

    private Ship currentShip;
    private Ship otherShip;

    private static final HashMap<Texture, List<Ship>> shipsByMap = new HashMap<>();
    private static final AxisAlignedBoundingBox shipAABB = new AxisAlignedBoundingBox(new Vector2f(), new Vector2f());
    private static final AxisAlignedBoundingBox mapBoundingBox = new AxisAlignedBoundingBox(new Vector2f(), new Vector2f());
    private static final Vector2f shipPos = new Vector2f();
    private static final Vector2f shipScale = new Vector2f();
    private static final Vector2f mapPos = new Vector2f();
    private static final Vector2f mapScale = new Vector2f();
    private static final Vector4f color = new Vector4f();
    private static final Vector2f weaponPos = new Vector2f();
    private static final Vector2f rotationVector = new Vector2f();

    private Button buttonControl;

    private final String openGlVersion = GL11.glGetString(GL11.GL_VERSION);
    private final String openGlRenderer = GL11.glGetString(GL11.GL_RENDERER);

    @Override
    protected void initElements() {
        int scaleX = 200;
        int scaleY = 157;
        hudShip.setSize(scaleX, scaleY);
        hudShip.atBottomRightCorner(-scaleX, -scaleY);
        registerGuiObject(hudShip);

        hudShipSecondary.setSize(scaleX, scaleY);
        hudShipSecondary.atTopRightCorner(-scaleX, 0);
        registerGuiObject(hudShipSecondary);

        map.setSize(scaleX, scaleY).atUpperLeftCorner(0, 0);
        registerGuiObject(map);

        scaleX = 100;
        scaleY = 51;
        hudShipAdd0.setSize(scaleX, scaleY);
        hudShipAdd0.atBottomRightCorner(-hudShip.getWidth() - scaleX, -scaleY);
        registerGuiObject(hudShipAdd0);

        scaleX = 256;
        scaleY = 136;
        chat.setSize(scaleX, scaleY);
        chat.atBottomLeftCorner(0, -scaleY);
        registerGuiObject(chat);

        chatInput.setSize((int) (scaleX / 1.05f), (int) (scaleY / 1.12f));
        chatInput.setStringOffset(new Vector2i(-66, 49));
        chatInput.setPosition(center.x - 516, center.y + 294);
        chatInput.setMaxLineSize(200);
        registerGuiObject(chatInput);

        controlText.setPosition(center.x + 525, center.y + 194);
        if (core.getWorld() != null && core.getWorld().getPlayerShip() == null) {
            controlText.update(Lang.getString("gui.control"));
        } else {
            controlText.update(Lang.getString("gui.cancelControl"));
        }
        registerGuiObject(controlText);

        createButton();
    }

    private void createButton() {
        int scaleX = 128;
        int scaleY = 20;
        buttonControl = new Button(TextureRegister.guiButtonControl, (int) (center.x + scaleX * 4.4f), (int) (center.y + scaleY * 10.0f), scaleX, scaleY);
        buttonControl.setOnMouseClickedRunnable(() -> {
            WorldClient w = core.getWorld();
            controlWasPressed = true;
            Ship playerControlledShip = w.getPlayerShip();
            if (playerControlledShip != null) {
                core.sendPacket(new PacketShipControl(playerControlledShip.getId(), false));
                core.getWorld().setPlayerShip(null);
                selectShip(playerControlledShip);
                cancelShipControl();
            } else if (canControlShip(currentShip)) {
                core.getWorld().setPlayerShip(currentShip);
                core.sendPacket(new PacketShipControl(currentShip.getId(), true));
            }
        });
        registerGuiObject(buttonControl);
        chatInput.addEmptyText();
        if (core.getWorld() != null && core.getWorld().getPlayerShip() == null) {
            controlText.update(Lang.getString("gui.control"));
        } else {
            controlText.update(Lang.getString("gui.cancelControl"));
        }

        controlButtonCreated = true;
    }

    public void addChatMessage(String message) {
        chatInput.addNewLineToChat(message);
    }

    @Override
    public void onMouseLeftClicked() {
        chatInput.onMouseLeftClick();
    }

    public boolean isActive() {
        return chatInput.isActive();
    }

    @Override
    public void input(int key) {
        super.input(key);
        chatInput.input(key);

        if (key == GLFW.GLFW_KEY_ESCAPE && Core.getCore().canControlShip()) {
            Core.getCore().setCurrentGui(new GuiInGameMenu());
        }
    }

    @Override
    public void textInput(int key) {
        if (chatInput.isTyping()) {
            chatInput.textInput(key);
        }
    }

    @Override
    public void onMouseLeftRelease() {
        super.onMouseLeftRelease();
        controlWasPressed = false;
        chatInput.onMouseLeftRelease();
    }

    @Override
    public void onMouseScroll(float y) {
        super.onMouseScroll(y);
        chatInput.scroll(y);
    }

    @Override
    public void update() {
        int yPos = (int) (6 + map.getHeight() * 0.93f);
        int xPos = 6;

        Profiler profiler = core.getProfiler();
        Profiler sProfiler = MainServer.getInstance() != null ? MainServer.getInstance().getProfiler() : null;
        float updateTime = profiler.getResult("update");
        float renderTime = profiler.getResult("render");
        int drawCalls = core.getRenderer().getDrawCalls();
        core.getRenderer().setDrawCalls(0);
        float physicsTime = profiler.getResult("physics");
        float netTime = profiler.getResult("network");
        float sUpdateTime = sProfiler != null ? sProfiler.getResult("update") : 0;
        float sPhysicsTime = sProfiler != null ? sProfiler.getResult("physics") : 0;
        float sNetworkTime = sProfiler != null ? sProfiler.getResult("network") : 0;
        Runtime var1 = Runtime.getRuntime();
        long maxMemory = var1.maxMemory();
        long totalMemory = var1.totalMemory();
        long freeMemory = var1.freeMemory();
        long maxMemoryMB = maxMemory / 1024L / 1024L;
        long totalMemoryMB = totalMemory / 1024L / 1024L;
        long freeMemoryMB = freeMemory / 1024L / 1024L;

        int tps = MainServer.getInstance() != null ? MainServer.getInstance().getUps() : 0;

        upperText.update("BFSR Client Dev 0.0.4 \n" +
                "FPS " + Core.getCore().getRenderer().getFps() + "/" + tps + " \n" +
                //"System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") v." + System.getProperty("os.version") + " \n" +
                //"Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + " \n" +
                //System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor") + " \n" +
                "Memory: " + (totalMemoryMB - freeMemoryMB) + "MB / " + totalMemoryMB + "MB up to " + maxMemoryMB + "MB \n" +
                "OpenGL: " + openGlRenderer + " \nversion " + openGlVersion + " \n" +

                " \n" +
                "Update: " + formatter.format(updateTime) + "ms / " + formatter.format(sUpdateTime) + "ms " +
                "\nPhysics: " + formatter.format(physicsTime) + "ms / " + formatter.format(sPhysicsTime) + "ms " +
                "\nRender: " + formatter.format(renderTime) + "ms " + drawCalls + " draw calls " +
                "\nNetwork: " + formatter.format(netTime) + "ms / " + formatter.format(sNetworkTime) + "ms " +
                "\nPing: " + ping);
        upperText.setPosition(xPos, yPos);
        yPos += yOffset * 11;
        World world = core.getWorld();
        if (world != null) {
            Camera cam = core.getRenderer().getCamera();
            Vector2f camPos = cam.getPosition();
            int bulletsCount = world.getBullets().size();
            int shipsCount = world.getShips().size();
            int particlesCount = world.getParticleRenderer().getParticles().size();
            int physicParticles = world.getParticleRenderer().getParticlesWrecks().size();

            WorldServer sWorld = MainServer.getInstance() != null ? MainServer.getInstance().getWorld() : null;
            int sBulletsCount = sWorld != null ? sWorld.getBullets().size() : 0;
            int sShipsCount = sWorld != null ? sWorld.getShips().size() : 0;
            int sParticlesCount = sWorld != null ? sWorld.getParticles().size() : 0;
            worldText.update("---World--- " +
                    "\nCamera pos: " + formatter.format(camPos.x) + ", " + formatter.format(camPos.y) + " " +
                    "\nShips count: " + shipsCount + "/" + sShipsCount +
                    " \nBullets count: " + bulletsCount + "/" + sBulletsCount +
                    " \nParticles count: " + particlesCount +
                    " \nPhysic particles count: " + physicParticles + "/" + sParticlesCount);
            worldText.setPosition(xPos, yPos);

            yPos += yOffset * 7;
            Ship playerShip = world.getPlayerShip();
            if (playerShip != null) {
                Vector2f pos = playerShip.getPosition();
                Vector2f velocity = playerShip.getVelocity();
                Hull hull = playerShip.getHull();
                Shield shield = playerShip.getShield();
                Reactor reactor = playerShip.getReactor();
                shipText.update("---Player Ship--- "
                        + "\nShip = " + playerShip.getClass().getSimpleName() + " \n" +
                        "Pos: " + formatter.format(pos.x) + ", " + formatter.format(pos.y) + " \n" +
                        "Velocity: " + formatter.format(velocity.x) + ", " + formatter.format(velocity.y) + " \n" +
                        "Mass: " + formatter.format(playerShip.getBody().getMass().getMass()) + " \n" +

                        "Hull: " + formatter.format(hull.getHull()) + "/" + formatter.format(hull.getMaxHull()) + " \n" +
                        "Shield: " + formatter.format(shield.getShield()) + "/" + formatter.format(shield.getMaxShield()) + " \n" +
                        "Reactor: " + formatter.format(reactor.getEnergy()) + "/" + formatter.format(reactor.getMaxEnergy()));
                shipText.setPosition(xPos, yPos);
            }

            if (currentShip != null) {
                if (canControlShip(currentShip)) {
                    if (!controlButtonCreated)
                        createButton();
                }

                if (currentShip.isDead()) currentShip = null;
            } else {
                if (controlButtonCreated) {
                    unregisterGuiObject(buttonControl);
                    controlButtonCreated = false;
                }
                controlText.clear();
                shipCrew.clear();
                shipCargo.clear();
            }

            if (otherShip != null) {
                if (otherShip.isDead()) otherShip = null;
            }
        }

        if (world != null) {
            super.update();
        }
    }

    private boolean canControlShip(Ship s) {
        return s.getName().equals(core.getPlayerName());
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

    private void renderMap(BaseShader shader, World world) {
        map.render(shader);
        List<Ship> ships = world.getShips();
        Vector2f camPos = core.getRenderer().getCamera().getPosition();
        float mapOffsetX = 600;
        float mapOffsetY = 600;
        mapBoundingBox.getMin().x = camPos.x - mapOffsetX;
        mapBoundingBox.getMin().y = camPos.y - mapOffsetY;
        mapBoundingBox.getMax().x = camPos.x + mapOffsetX;
        mapBoundingBox.getMax().y = camPos.y + mapOffsetY;
        mapPos.x = center.x - 550;
        mapPos.y = center.y - 286.0f;
        float mapScaleX = 5.0f;
        float mapScaleY = 7.0f;
        float shipSize = 1.0f;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        mapScale.x = this.map.getWidth();
        mapScale.y = this.map.getHeight();
        int sizeX = (int) (mapScale.x);
        int sizeY = (int) (mapScale.y);
        mapScale.x /= 2.0f;
        mapScale.y /= 2.0f;
        int x = (int) (map.getX() - mapScale.x);
        int y = (int) (map.getY() + mapScale.y);
        int offsetY = (int) (17 * Transformation.guiScale.y);
        int offsetX = (int) (19 * Transformation.guiScale.x);
        GL11.glScissor(x + offsetX, height - y + offsetY, sizeX - offsetX * 2, sizeY - offsetY * 2);
        color.x = color.y = color.z = color.w = 1;
        shipsByMap.clear();

        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            Vector2f pos = s.getPosition();
            Vector2f scale = s.getScale();
            shipScale.x = scale.x;
            shipScale.y = scale.y;
            shipScale.x /= shipSize;
            shipScale.y /= shipSize;
            float sX = shipScale.x / 2.0f;
            float sY = shipScale.y / 2.0f;
            shipAABB.setMinX(pos.x - sX);
            shipAABB.setMaxX(pos.x + sX);
            shipAABB.setMinY(pos.y - sY);
            shipAABB.setMaxY(pos.y + sY);
            if (mapBoundingBox.isIntersects(shipAABB)) {
                Texture t = s.getTexture();
                List<Ship> ss = shipsByMap.get(t);
                if (ss == null) ss = new ArrayList<>();
                ss.add(s);
                shipsByMap.put(t, ss);
            }
        }

        for (List<Ship> ss : shipsByMap.values()) {
            for (Ship s : ss) {
                Vector2f pos = s.getPosition();
                Vector2f scale = s.getScale();
                shipScale.x = scale.x;
                shipScale.y = scale.y;
                shipScale.x /= shipSize;
                shipScale.y /= shipSize;
                Faction faction = s.getFaction();
                if (faction == Faction.Engi) {
                    color.x = 0.5f;
                    color.y = 1.0f;
                    color.z = 0.5f;
                } else if (faction == Faction.Human) {
                    color.x = 0.5f;
                    color.y = 0.5f;
                    color.z = 1.0f;
                } else {
                    color.x = 1.0f;
                    color.y = 1.0f;
                    color.z = 0.5f;
                }

                shipPos.x = mapPos.x + (pos.x - camPos.x) / mapScaleX;
                shipPos.y = mapPos.y + (pos.y - camPos.y) / mapScaleY;
                renderQuad(shader, color, s.getTexture(), shipPos, s.getRotation(), shipScale);
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void renderShipHud(BaseShader shader, World world, Ship ship, boolean isSecondary) {
        if (ship != null) {
            OpenGLHelper.alphaGreater(0.01f);
            if (!isSecondary) hudShip.render(shader);
            else hudShipSecondary.render(shader);
            color.x = color.y = color.z = 0;
            color.w = 1;
            OpenGLHelper.alphaGreater(0.75f);
            if (!isSecondary) {
                shipPos.x = center.x + 550;
                shipPos.y = center.y + 286;
            } else {
                shipPos.x = center.x + 550;
                shipPos.y = center.y - 286;
            }
            float shipSize = 0.5f;
            shipScale.x = ship.getScale().x;
            shipScale.y = ship.getScale().y;
            shipScale.mul(shipSize);

            color.y = ship.getHull().getHull() / ship.getHull().getMaxHull();
            color.x = 1.0f - color.y;
            color.z = 0.0f;
            renderQuad(shader, color, ship.getTexture(), shipPos, (float) (-Math.PI / 2.0f), shipScale);
            Texture tShield = TextureLoader.getTexture(TextureRegister.shieldSmall0);
            if (!isSecondary) {
                String string = String.valueOf(Math.round(ship.getHull().getHull()));
                textHull.update(string);
                textHull.setPosition((int) shipPos.x, (int) (shipPos.y + 16));
                color.x = 0;
                color.y = 0;
                color.z = 0;
                shipScale.x = 12;
                shipScale.y = 6 * string.length() + 6;
                OpenGLHelper.alphaGreater(0.01f);
                shipPos.y += 15;
                renderQuad(shader, color, tShield, shipPos, (float) (-Math.PI / 2.0f), shipScale);
                shipPos.y -= 15;
            }

            Shield shield = ship.getShield();
            if (shield != null && shield.shieldAlive()) {
                color.y = shield.getShield() / shield.getMaxShield();
                color.x = 1.0f - color.y;
                color.z = 0;
                shipScale.x = 140.0f * shield.getSize();
                shipScale.y = 140.0f * shield.getSize();
                renderQuad(shader, color, this.shield.getTexture(), shipPos, (float) (-Math.PI), shipScale);
                if (!isSecondary) {
                    String string = String.valueOf(Math.round(shield.getShield()));
                    textShield.update(string);
                    textShield.setPosition((int) shipPos.x, (int) (shipPos.y + 48));
                    color.x = 0;
                    color.y = 0;
                    color.z = 0;
                    shipScale.x = 12;
                    shipScale.y = 6 * string.length() + 6;
                    shipPos.y += 47;
                    renderQuad(shader, color, tShield, shipPos, (float) (-Math.PI / 2.0f), shipScale);
                    shipPos.y -= 47;
                }
            } else {
                textShield.clear();
            }

            Armor armor = ship.getArmor();
            ArmorPlate[] plates = armor.getArmorPlates();
            float rot = (float) Math.PI;
            shipScale.x = this.armorPlate.getWidth() / 1.8f;
            shipScale.y = this.armorPlate.getHeight() / 1.8f;
            color.x = 1.0f;
            color.z = 0.0f;
            for (int i = 0; i < 4; i++) {
                ArmorPlate plate = plates[i];
                rot -= Math.PI / 2.0;
                if (plate != null) {
                    RotationHelper.rotate(rot, -35, 0, rotationVector);
                    rotationVector.x += shipPos.x;
                    rotationVector.y += shipPos.y;
                    color.y = plate.getArmor() / plate.getArmorMax();
                    color.x = 1.0f - color.y;
                    renderQuad(shader, color, this.armorPlate.getTexture(), rotationVector, (float) (rot + Math.PI), shipScale);
                }
            }

            if (!isSecondary && canControlShip(ship)) {
                Texture energyText = energy.getTexture();
                shipScale.x = energy.getWidth() * 0.6f;
                shipScale.y = energy.getHeight() * 0.6f;
                Reactor reactor = ship.getReactor();
                float energy = reactor.getEnergy() / reactor.getMaxEnergy() * 20.0f;
                for (int i = 0; i < 20; i++) {
                    rot = (float) (i * 0.08f - Math.PI / 4.0f);
                    RotationHelper.rotate(rot, -70, 0, rotationVector);
                    rotationVector.x += shipPos.x;
                    rotationVector.y += shipPos.y;
                    color.x = 0;
                    color.y = 0;
                    color.z = 0;
                    renderQuad(shader, color, energyText, rotationVector, (float) (-Math.PI + rot), shipScale);
                    if (energy >= i) {
                        color.x = 0.25f;
                        color.y = 0.5f;
                        color.z = 1.0f;
                        renderQuad(shader, color, energyText, rotationVector, (float) (-Math.PI + rot), shipScale);
                    }
                }
                OpenGLHelper.alphaGreater(0.01f);
                hudShipAdd0.render(shader);

                shipCargo.setPosition(center.x + 380, center.y + 320);
                shipCargo.update(Lang.getString(Lang.getString("gui.shipCargo") + ": " + ship.getCargo().getCapacity() + "/" + ship.getCargo().getMaxCapacity()));

                shipCrew.setPosition(center.x + 380, center.y + 330);
                shipCrew.update(Lang.getString(Lang.getString("gui.shipCrew") + ": " + ship.getCrew().getCrewSize() + "/" + ship.getCrew().getMaxCrewSize()));
            }

            OpenGLHelper.alphaGreater(0.75f);
            for (WeaponSlot slot : ship.getWeaponSlots()) {
                if (slot != null) {
                    float reload = slot.getShootTimer() / slot.getShootTimerMax();
                    color.x = reload;
                    color.y = reload;
                    color.z = 1.0f;
                    Vector2f pos = slot.getAddPosition();
                    RotationHelper.rotate((float) (-Math.PI / 2.0f), pos.x, pos.y, rotationVector);
                    weaponPos.x = shipPos.x + rotationVector.x * shipSize;
                    weaponPos.y = shipPos.y + rotationVector.y * shipSize;
                    shipScale.x = slot.getScale().x;
                    shipScale.y = slot.getScale().y;
                    shipScale.mul(shipSize);
                    renderQuad(shader, color, slot.getTexture(), weaponPos, (float) (-Math.PI / 2.0f), shipScale);
                }
            }

        }
    }

    void renderChat(BaseShader shader) {
        OpenGLHelper.alphaGreater(0.01f);
        chat.render(shader);
        chatInput.render(shader);
    }

    @Override
    public void render(BaseShader shader) {
        if (core.getWorld() != null) {
            OpenGLHelper.alphaGreater(0.01f);
            super.render(shader);
        }

        World world = core.getWorld();

        if (world != null) {
            OpenGLHelper.alphaGreater(0.01f);
            renderMap(shader, world);
            renderShipHud(shader, world, currentShip, false);
            renderShipHud(shader, world, otherShip, true);
            renderChat(shader);
        }
    }

    private void renderQuad(BaseShader shader, Vector4f color, Texture texture, Vector2f pos, float rot, Vector2f scale) {
        Vector2f pos1 = Transformation.getOffsetByScale(pos);
        shader.setColor(color.x, color.y, color.z, color.w);
        shader.setModelMatrix(Transformation.getModelViewMatrixGui(pos1.x, pos1.y, rot, scale.x * Transformation.guiScale.x, scale.y * Transformation.guiScale.y).get(ShaderProgram.MATRIX_BUFFER));
        if (texture != null) texture.bind();
        Renderer.centeredQuad.renderIndexed();
    }

    public void clearByExit() {
        super.clear();
        controlButtonCreated = false;
    }

    @Override
    public void clear() {
        super.clear();
    }
}
