package net.bfsr.client.gui.ingame;

import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.BlankGuiObject;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.util.DecimalUtils;
import net.bfsr.world.World;
import org.jbox2d.common.Vector2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.List;
import java.util.function.Consumer;

public class DebugInfoElement extends MinimizableGuiObject {
    private static final Font FONT_TYPE = Font.CONSOLA_FT;
    private static final int FONT_SIZE = 13;

    private final StringBuilder stringBuilder = new StringBuilder(64);
    private final String openGlVersion = Engine.renderer.glGetString(GL.GL_VERSION);
    private final String openGlRenderer = Engine.renderer.glGetString(GL.GL_RENDERER);
    @Setter
    private float ping;

    private final Client client = Client.get();
    private final ParticleRenderer particleRenderer = client.getGlobalRenderer().getParticleRenderer();
    private final PlayerInputController playerInputController = client.getInputHandler().getPlayerInputController();
    private final AbstractMouse mouse = Engine.mouse;
    private int sortTimer;
    private final StringBuilder offset = new StringBuilder(32);
    private final StringBuilder fullCategoryName = new StringBuilder(32);
    private final ScrollPane scrollPane = new ScrollPane(300 - STATIC_STRING_X_OFFSET, 500, 10);
    private final Label profilerLabel = new Label(FONT_TYPE, "", 0, 0, FONT_SIZE);

    public DebugInfoElement(HUD hud) {
        super(300, 20, "Debug info", FONT_TYPE, FONT_SIZE, 0, 0, MINIMIZABLE_STRING_X_OFFSET,
                STATIC_STRING_X_OFFSET);

        setTextColor(205 / 255.0f, 205 / 255.0f, 205 / 255.0f, 1.0f).setHoverColor(0.3f, 0.3f, 0.3f, 0.5f);

        add(scrollPane);
        scrollPane.setScrollColor(0.5f, 0.5f, 0.5f, 0.25f);
        scrollPane.setScrollHoverColor(0.5f, 0.5f, 0.5f, 0.5f);

        int width = 300 - STATIC_STRING_X_OFFSET - 10;
        int height = 20;
        int y = 0;
        y -= addDebugLabel(y, "BFSR Client " + Client.GAME_VERSION + "\n", label1 -> {}).getHeight();
        y -= addDebugLabel(y, "", label1 -> {
            ServerGameLogic server = ServerGameLogic.getInstance();
            int ups = server != null ? server.getUps() : 0;
            label1.setString("FPS " + Engine.renderer.getFps() + ", Local Server UPS " + ups);
        }).getHeight();
        y -= addDebugLabel(y, "", label1 -> {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long maxMemoryMB = maxMemory / 1024L / 1024L;
            long totalMemoryMB = totalMemory / 1024L / 1024L;
            long freeMemoryMB = freeMemory / 1024L / 1024L;
            label1.setString("Memory: " + (totalMemoryMB - freeMemoryMB) + "MB / " + totalMemoryMB +
                    "MB up to " + maxMemoryMB + "MB");
        }).getHeight();
        y -= addDebugLabel(y, "", label1 -> {
            Vector2f mousePosition = mouse.getPosition();
            label1.setString("Mouse screen pos: " + (int) mousePosition.x + ", " + (int) mousePosition.y);
        }).getHeight();
        y -= addDebugLabel(y, "", label1 -> {
            AbstractCamera camera = Engine.renderer.camera;
            Vector2f mouseWorldPosition = mouse.getWorldPosition(camera);
            label1.setString("Mouse world pos: " + DecimalUtils.strictFormatWithToDigits(mouseWorldPosition.x) +
                    ", " + DecimalUtils.strictFormatWithToDigits(mouseWorldPosition.y));
        }).getHeight();
        y -= addMinimizableWithLabel(width, height, y, "Profiler", profilerLabel).getHeight();
        y -= addMinimizableWithLabel(width, height, y, "Network", createLabel(0, "",
                label1 -> label1.setString("Ping: " + DecimalUtils.strictFormatWithToDigits(ping) + "ms" +
                        "\nClient render delay: " + client.getClientRenderDelay() / 1_000_000 + "ms"))).getHeight();
        y -= addMinimizableWithLabel(width, height, y, "Render", createLabel(0, "",
                label1 -> {
                    AbstractCamera camera = Engine.renderer.camera;
                    Vector2f camPos = camera.getPosition();
                    label1.setString("GPU: " + openGlRenderer +
                            "\nDriver version: " + openGlVersion +
                            "\nCamera pos: " + DecimalUtils.strictFormatWithToDigits(camPos.x) + ", " +
                            DecimalUtils.strictFormatWithToDigits(camPos.y) +
                            "\nDraw calls: " + Engine.renderer.getLastFrameDrawCalls() +
                            "\nParticle Renderer: " +
                            (particleRenderer.getTaskCount() > 1 ? particleRenderer.getTaskCount() + " active threads" :
                                    "single-threaded"));
                })).getHeight();
        y -= addMinimizableWithLabel(width, height, y, "World", createLabel(0, "",
                label1 -> {
                    World world = client.getWorld();
                    int bulletsCount = world.getBulletsCount();
                    int shipsCount = world.getEntitiesByType(Ship.class).size();
                    int particlesCount = client.getParticlesCount();
                    int wreckCount = world.getWreckCount();
                    int shipWreckCount = world.getShipWreckCount();
                    int bodyCount = world.getPhysicWorld().getBodyCount();

                    ServerGameLogic server = ServerGameLogic.getInstance();
                    int sBulletsCount = 0;
                    int sShipsCount = 0;
                    int sWrecksCount = 0;
                    int sShipWrecksCount = 0;
                    int sBodyCount = 0;
                    if (server != null) {
                        World sWorld = server.getWorld();
                        sBulletsCount = sWorld.getBulletsCount();
                        sShipsCount = sWorld.getEntitiesByType(Ship.class).size();
                        sWrecksCount = sWorld.getWreckCount();
                        sShipWrecksCount = sWorld.getShipWreckCount();
                        sBodyCount = sWorld.getPhysicWorld().getBodyCount();
                    }

                    label1.setString("Physic body count: " + bodyCount + "/" + sBodyCount +
                            "\nShips count: " + shipsCount + "/" + sShipsCount +
                            "\nBullets count:" + bulletsCount + "/" + sBulletsCount +
                            "\nParticles count: " + particlesCount +
                            "\nWrecks count: " + wreckCount + "/" + sWrecksCount +
                            "\nShip wrecks count: " + shipWreckCount + "/" + sShipWrecksCount);
                })).getHeight();
        y -= addMinimizableWithLabel(width, height, y, "Player ship", createLabel(0, "",
                label1 -> {
                    Ship playerShip = playerInputController.getShip();
                    if (playerShip != null) {
                        Vector2 velocity = playerShip.getLinearVelocity();
                        Shield shield = playerShip.getModules().getShield();
                        Reactor reactor = playerShip.getModules().getReactor();

                        label1.setString("Ship: " + playerShip.getClass().getSimpleName() +
                                "\nPos: " + DecimalUtils.strictFormatWithToDigits(playerShip.getX()) + ", " +
                                DecimalUtils.strictFormatWithToDigits(playerShip.getY()) +
                                "\nVelocity:" + DecimalUtils.strictFormatWithToDigits(velocity.x) + ", " +
                                DecimalUtils.strictFormatWithToDigits(velocity.y) +
                                "\nMass: " + DecimalUtils.strictFormatWithToDigits(playerShip.getBody().getMass()) +
                                "\nShield: " + DecimalUtils.strictFormatWithToDigits(shield.getShieldHp()) + "/" +
                                DecimalUtils.strictFormatWithToDigits(shield.getMaxHp()) +
                                "\nReactor: " + DecimalUtils.strictFormatWithToDigits(reactor.getEnergy()) + "/" +
                                DecimalUtils.strictFormatWithToDigits(reactor.getMaxEnergy()));
                    } else {
                        label1.setString("");
                    }
                })).getHeight();
        addMinimizableWithLabel(width, height, y, "Selected Ship", createLabel(0, "",
                label1 -> {
                    Ship ship = hud.getSelectedShip();
                    if (ship != null) {
                        label1.setString("Id: " + ship.getId());
                    } else {
                        label1.setString("");
                    }
                }));
    }

    private Label addDebugLabel(int y, String text, Consumer<Label> updateConsumer) {
        Label label = createLabel(y, text, updateConsumer);
        scrollPane.add(label);
        return label;
    }

    private Label createLabel(int y, String text, Consumer<Label> updateConsumer) {
        return new Label(Font.CONSOLA_FT, text, 0, 0, FONT_SIZE) {
            @Override
            public void update() {
                super.update();
                updateConsumer.accept(this);
            }
        }.atTopLeft(0, y);
    }

    private MinimizableGuiObject addMinimizableWithLabel(int width, int height, int y, String name, Label label) {
        MinimizableGuiObject minimizableGuiObject = new MinimizableGuiObject(width, height, name, FONT_TYPE, FONT_SIZE,
                0, 0, MINIMIZABLE_STRING_X_OFFSET, STATIC_STRING_X_OFFSET);
        scrollPane.add(minimizableGuiObject.atTopLeft(0, y).setTextColor(205 / 255.0f, 205 / 255.0f, 205 / 255.0f, 1.0f)
                .setHoverColor(0.3f, 0.3f, 0.3f, 0.5f));
        minimizableGuiObject.add(label);
        return minimizableGuiObject;
    }

    @Override
    public void update() {
        super.update();

        if (!ClientSettings.IS_DEBUG.getBoolean() || profilerLabel.getParent() == BlankGuiObject.INSTANCE) return;

        if (((MinimizableGuiObject) profilerLabel.getParent()).isMaximized()) {
            stringBuilder.setLength(0);
            Profiler profiler = client.getProfiler();
            offset.setLength(0);
            fullCategoryName.setLength(0);
            fullCategoryName.append("root");
            boolean needSort = sortTimer-- == 0;
            @Nullable Profiler serverProfiler;

            ServerGameLogic server = ServerGameLogic.getInstance();
            if (server != null) {
                serverProfiler = server.getProfiler();
            } else {
                serverProfiler = null;
            }

            profiler.getResults(needSort).compute(node -> {
                float serverResult = 0.0f;

                if (serverProfiler != null) {
                    serverResult = serverProfiler.getResult(fullCategoryName + "." + node.getName());
                }

                stringBuilder.append(offset).append(node.getName()).append(": ")
                        .append(DecimalUtils.strictFormatWithToDigits(node.getAverageTime()))
                        .append("ms");

                if (serverResult > 0.0f) {
                    stringBuilder.append(" / ").append(DecimalUtils.strictFormatWithToDigits(serverResult)).append("ms");
                }

                stringBuilder.append("\n");
            }, (node) -> {
                offset.append(" ");
                fullCategoryName.append(".").append(node.getName());
            }, (node) -> {
                offset.deleteCharAt(offset.length() - 1);
                fullCategoryName.delete(fullCategoryName.lastIndexOf("."), fullCategoryName.length());
            });

            profilerLabel.setString(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());

            if (needSort) {
                sortTimer = 60;
            }
        }
    }

    @Override
    protected void onChildSizeChanged(GuiObject guiObject, int width, int height) {
        super.onChildSizeChanged(guiObject, width, height);
        updatePositionAndSize();
    }

    @Override
    public void updatePositionAndSize() {
        updatePositions();
        super.updatePositionAndSize();
    }

    private void updatePositions() {
        List<GuiObject> guiObjects = scrollPane.getGuiObjects();
        for (int i = 0, y = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            guiObject.atTopLeft(0, y);
            y -= guiObject.getHeight();
        }
    }
}