package net.bfsr.client.gui.ingame;

import lombok.Setter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.renderer.Renderer;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.particle.ParticleRenderer;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.MainServer;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.DecimalUtils;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

public class DebugInfoElement {
    private final StringBuilder stringBuilder = new StringBuilder(64);
    private final String openGlVersion = GL11.glGetString(GL11.GL_VERSION);
    private final String openGlRenderer = GL11.glGetString(GL11.GL_RENDERER);
    @Setter
    private float ping;
    private final StringObject stringObject = new StringObject(FontType.CONSOLA);

    public void init(int x, int y) {
        stringObject.setPosition(x, y);
    }

    public void update() {
        Core core = Core.get();

        Renderer renderer = core.getRenderer();
        int drawCalls = renderer.getLastFrameDrawCalls();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemoryMB = maxMemory / 1024L / 1024L;
        long totalMemoryMB = totalMemory / 1024L / 1024L;
        long freeMemoryMB = freeMemory / 1024L / 1024L;

        int ups = MainServer.getInstance() != null ? MainServer.getInstance().getUps() : 0;
        ParticleRenderer particleRenderer = renderer.getParticleRenderer();

        stringBuilder.setLength(0);
        stringBuilder.append("BFSR Client Dev 0.0.4 \n");
        stringBuilder.append("FPS ").append(renderer.getFps()).append(", Local Server UPS ").append(ups);
        stringBuilder.append("\nMemory: ").append(totalMemoryMB - freeMemoryMB).append("MB / ").append(totalMemoryMB).append("MB up to ").append(maxMemoryMB).append("MB");
        stringBuilder.append("\nMouse screen pos: ");
        Vector2f mousePosition = Mouse.getPosition();
        stringBuilder.append((int) mousePosition.x).append(", ").append((int) mousePosition.y);
        Camera camera = renderer.getCamera();
        Vector2f mouseWorldPosition = Mouse.getWorldPosition(camera);
        stringBuilder.append("\nMouse world pos: ").append(DecimalUtils.formatWithToDigits(mouseWorldPosition.x)).append(", ").append(DecimalUtils.formatWithToDigits(mouseWorldPosition.y));
        stringBuilder.append("\n\n---Profiler---");
        Profiler profiler = core.getProfiler();
        float updateTime = profiler.getResult("update");
        float renderTime = profiler.getResult("render");
        float physicsTime = profiler.getResult("physics");
        float netTime = profiler.getResult("network");
        float sUpdateTime = 0.0f;
        float sPhysicsTime = 0.0f;
        float sNetworkTime = 0.0f;

        if (MainServer.getInstance() != null) {
            sUpdateTime = MainServer.getInstance().getProfiler().getResult("update");
            sPhysicsTime = MainServer.getInstance().getProfiler().getResult("physics");
            sNetworkTime = MainServer.getInstance().getProfiler().getResult("network");
        }
        stringBuilder.append("\nUpdate: ").append(DecimalUtils.formatWithToDigits(updateTime)).append("ms / ").append(DecimalUtils.formatWithToDigits(sUpdateTime)).append("ms ");
        stringBuilder.append("\nPhysics: ").append(DecimalUtils.formatWithToDigits(physicsTime)).append("ms / ").append(DecimalUtils.formatWithToDigits(sPhysicsTime)).append("ms ");
        stringBuilder.append("\nRender: ").append(DecimalUtils.formatWithToDigits(renderTime)).append("ms ");
        stringBuilder.append("\nNetwork: ").append(DecimalUtils.formatWithToDigits(netTime)).append("ms / ").append(DecimalUtils.formatWithToDigits(sNetworkTime)).append("ms ");
        stringBuilder.append("\nPing: ").append(DecimalUtils.formatWithToDigits(ping)).append("ms");

        stringBuilder.append("\n\n---Render---");
        stringBuilder.append("\nGPU: ").append(openGlRenderer);
        stringBuilder.append(" \nDrivers version ").append(openGlVersion);
        Vector2f camPos = camera.getPosition();
        stringBuilder.append("\nCamera pos: ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(camPos.x)).append(", ").append(DecimalUtils.formatWithToDigits(camPos.y));
        stringBuilder.append("\nDraw calls: ").append(drawCalls);
        stringBuilder.append("\nParticle Renderer: ");
        stringBuilder.append(particleRenderer.getTaskCount() > 1 ? particleRenderer.getTaskCount() + " active threads" : "single-threaded");

        WorldClient world = core.getWorld();
        if (world != null) {
            int bulletsCount = world.getBullets().size();
            int shipsCount = world.getShips().size();
            int particlesCount = particleRenderer.getParticlesCount();
            int physicParticles = world.getParticleManager().getWreckCount();

            WorldServer sWorld = MainServer.getInstance() != null ? MainServer.getInstance().getWorld() : null;
            int sBulletsCount = sWorld != null ? sWorld.getBullets().size() : 0;
            int sShipsCount = sWorld != null ? sWorld.getShips().size() : 0;
            int sParticlesCount = sWorld != null ? sWorld.getParticles().size() : 0;
            stringBuilder.append("\n\n---World--- ");
            stringBuilder.append("\nShips count: ").append(shipsCount).append("/").append(sShipsCount);
            stringBuilder.append(" \nBullets count: ").append(bulletsCount).append("/").append(sBulletsCount);
            stringBuilder.append(" \nParticles count: ").append(particlesCount);
            stringBuilder.append(" \nPhysic particles count: ").append(physicParticles).append("/").append(sParticlesCount);

            Ship playerShip = world.getPlayerShip();
            if (playerShip != null) {
                Vector2f pos = playerShip.getPosition();
                Vector2f velocity = playerShip.getVelocity();
                Hull hull = playerShip.getHull();
                ShieldCommon shield = playerShip.getShield();
                Reactor reactor = playerShip.getReactor();
                stringBuilder.append("\n\n---Player Ship--- ");
                stringBuilder.append("\nShip = ").append(playerShip.getClass().getSimpleName());
                stringBuilder.append("\nPos: ").append(DecimalUtils.formatWithToDigits(pos.x)).append(", ").append(DecimalUtils.formatWithToDigits(pos.y));
                stringBuilder.append("\nVelocity: ").append(DecimalUtils.formatWithToDigits(velocity.x)).append(", ").append(DecimalUtils.formatWithToDigits(velocity.y));
                stringBuilder.append("\nMass: ").append(DecimalUtils.formatWithToDigits(playerShip.getBody().getMass().getMass()));
                stringBuilder.append("\nHull: ").append(DecimalUtils.formatWithToDigits(hull.getHull())).append("/").append(DecimalUtils.formatWithToDigits(hull.getMaxHull()));
                stringBuilder.append("\nShield: ").append(DecimalUtils.formatWithToDigits(shield.getShield())).append("/").append(DecimalUtils.formatWithToDigits(shield.getMaxShield()));
                stringBuilder.append("\nReactor: ").append(DecimalUtils.formatWithToDigits(reactor.getEnergy())).append("/").append(DecimalUtils.formatWithToDigits(reactor.getMaxEnergy()));
            }
        }

        stringObject.update(stringBuilder.toString());
    }

    public void render() {
        stringObject.render();
    }
}
