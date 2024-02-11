package net.bfsr.client.gui.ingame;

import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.util.DecimalUtils;
import net.bfsr.world.World;
import org.joml.Vector2f;

public class DebugInfoElement {
    private final StringBuilder stringBuilder = new StringBuilder(64);
    private final String openGlVersion = Engine.renderer.glGetString(GL.GL_VERSION);
    private final String openGlRenderer = Engine.renderer.glGetString(GL.GL_RENDERER);
    @Setter
    private float ping;
    private final StringObject stringObject = new StringObject(FontType.CONSOLA);
    private final Core core = Core.get();
    private final AbstractRenderer renderer = Engine.renderer;
    private final ParticleRenderer particleRenderer = core.getGlobalRenderer().getParticleRenderer();
    private final GuiManager guiManager = core.getGuiManager();
    private final PlayerInputController playerInputController = core.getInputHandler().getPlayerInputController();
    private final AbstractMouse mouse = Engine.mouse;

    public void init(int x, int y) {
        stringObject.setPosition(x, y);
    }

    public void update() {
        int drawCalls = renderer.getLastFrameDrawCalls();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemoryMB = maxMemory / 1024L / 1024L;
        long totalMemoryMB = totalMemory / 1024L / 1024L;
        long freeMemoryMB = freeMemory / 1024L / 1024L;

        ServerGameLogic server = ServerGameLogic.getInstance();
        int ups = server != null ? server.getUps() : 0;

        stringBuilder.setLength(0);
        stringBuilder.append("BFSR Client " + Core.GAME_VERSION + "\n");
        stringBuilder.append("FPS ").append(renderer.getFps()).append(", Local Server UPS ").append(ups);
        stringBuilder.append("\nMemory: ").append(totalMemoryMB - freeMemoryMB).append("MB / ").append(totalMemoryMB)
                .append("MB up to ").append(maxMemoryMB).append("MB");
        stringBuilder.append("\nMouse screen pos: ");
        Vector2f mousePosition = mouse.getPosition();
        stringBuilder.append((int) mousePosition.x).append(", ").append((int) mousePosition.y);
        AbstractCamera camera = renderer.camera;
        Vector2f mouseWorldPosition = mouse.getWorldPosition(camera);
        stringBuilder.append("\nMouse world pos: ").append(DecimalUtils.strictFormatWithToDigits(mouseWorldPosition.x))
                .append(", ").append(DecimalUtils.strictFormatWithToDigits(mouseWorldPosition.y));
        stringBuilder.append("\n\n---Profiler---");
        Profiler profiler = core.getProfiler();
        float updateTime = profiler.getResult("update");
        float renderTime = profiler.getResult("render");
        float physicsTime = profiler.getResult("physics");
        float netTime = profiler.getResult("network");
        float sUpdateTime = 0.0f;
        float sPhysicsTime = 0.0f;
        float sNetworkTime = 0.0f;

        if (server != null) {
            sUpdateTime = server.getProfiler().getResult("update");
            sPhysicsTime = server.getProfiler().getResult("physics");
            sNetworkTime = server.getProfiler().getResult("network");
        }
        stringBuilder.append("\nUpdate: ").append(DecimalUtils.strictFormatWithToDigits(updateTime)).append("ms / ")
                .append(DecimalUtils.strictFormatWithToDigits(sUpdateTime)).append("ms ");
        stringBuilder.append("\nPhysics: ").append(DecimalUtils.strictFormatWithToDigits(physicsTime)).append("ms / ")
                .append(DecimalUtils.strictFormatWithToDigits(sPhysicsTime)).append("ms ");
        stringBuilder.append("\nRender: ").append(DecimalUtils.strictFormatWithToDigits(renderTime)).append("ms ");
        stringBuilder.append("\nNetwork: ").append(DecimalUtils.strictFormatWithToDigits(netTime)).append("ms / ")
                .append(DecimalUtils.strictFormatWithToDigits(sNetworkTime)).append("ms ");
        stringBuilder.append("\nPing: ").append(DecimalUtils.strictFormatWithToDigits(ping)).append("ms");

        stringBuilder.append("\n\n---Render---");
        stringBuilder.append("\nGPU: ").append(openGlRenderer);
        stringBuilder.append(" \nDrivers version ").append(openGlVersion);
        Vector2f camPos = camera.getPosition();
        stringBuilder.append("\nCamera pos: ");
        stringBuilder.append(DecimalUtils.strictFormatWithToDigits(camPos.x)).append(", ")
                .append(DecimalUtils.strictFormatWithToDigits(camPos.y));
        stringBuilder.append("\nDraw calls: ").append(drawCalls);
        stringBuilder.append("\nParticle Renderer: ");
        stringBuilder.append(
                particleRenderer.getTaskCount() > 1 ? particleRenderer.getTaskCount() + " active threads" : "single-threaded");

        World world = core.getWorld();
        int bulletsCount = world.getBulletsCount();
        int shipsCount = world.getEntitiesByType(Ship.class).size();
        int particlesCount = core.getParticlesCount();
        int wreckCount = world.getWreckCount();
        int shipWreckCount = world.getShipWreckCount();
        int bodyCount = world.getPhysicWorld().getBodyCount();

        World sWorld = server != null ? server.getWorld() : null;
        int sBulletsCount = sWorld != null ? sWorld.getBulletsCount() : 0;
        int sShipsCount = sWorld != null ? sWorld.getEntitiesByType(Ship.class).size() : 0;
        int sWrecksCount = sWorld != null ? sWorld.getWreckCount() : 0;
        int sShipWrecksCount = sWorld != null ? sWorld.getShipWreckCount() : 0;
        int sBodyCount = sWorld != null ? sWorld.getPhysicWorld().getBodyCount() : 0;
        stringBuilder.append("\n\n---World--- ");
        stringBuilder.append("\nPhysic body count: ").append(bodyCount).append("/").append(sBodyCount);
        stringBuilder.append("\nShips count: ").append(shipsCount).append("/").append(sShipsCount);
        stringBuilder.append("\nBullets count: ").append(bulletsCount).append("/").append(sBulletsCount);
        stringBuilder.append("\nParticles count: ").append(particlesCount);
        stringBuilder.append("\nWrecks count: ").append(wreckCount).append("/").append(sWrecksCount);
        stringBuilder.append("\nShip wrecks count: ").append(shipWreckCount).append("/").append(sShipWrecksCount);

        Ship playerShip = playerInputController.getShip();
        if (playerShip != null) {
            Vector2f pos = playerShip.getPosition();
            Vector2f velocity = playerShip.getVelocity();
            Shield shield = playerShip.getModules().getShield();
            Reactor reactor = playerShip.getModules().getReactor();
            stringBuilder.append("\n\n---Player Ship--- ");
            stringBuilder.append("\nShip: ").append(playerShip.getClass().getSimpleName());
            stringBuilder.append("\nPos: ").append(DecimalUtils.strictFormatWithToDigits(pos.x)).append(", ")
                    .append(DecimalUtils.strictFormatWithToDigits(pos.y));
            stringBuilder.append("\nVelocity: ").append(DecimalUtils.strictFormatWithToDigits(velocity.x)).append(", ")
                    .append(DecimalUtils.strictFormatWithToDigits(velocity.y));
            stringBuilder.append("\nMass: ")
                    .append(DecimalUtils.strictFormatWithToDigits(playerShip.getBody().getMass().getMass()));
            stringBuilder.append("\nShield: ").append(DecimalUtils.strictFormatWithToDigits(shield.getShieldHp())).append("/")
                    .append(DecimalUtils.strictFormatWithToDigits(shield.getMaxHp()));
            stringBuilder.append("\nReactor: ").append(DecimalUtils.strictFormatWithToDigits(reactor.getEnergy())).append("/")
                    .append(DecimalUtils.strictFormatWithToDigits(reactor.getMaxEnergy()));
        }

        Ship ship = guiManager.getHud().getSelectedShip();
        if (ship != null) {
            stringBuilder.append("\n\n---Selected Ship--- ");
            stringBuilder.append("\nId: ").append(ship.getId());
        }

        stringObject.setStringAndCompile(stringBuilder.toString());
    }

    public void render() {
        stringObject.renderNoInterpolation();
    }
}