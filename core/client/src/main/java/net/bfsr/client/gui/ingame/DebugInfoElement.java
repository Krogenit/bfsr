package net.bfsr.client.gui.ingame;

import lombok.Setter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.Ship;
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
    private long ping;
    private final StringObject upperText = new StringObject(FontType.CONSOLA);
    private final StringObject worldText = new StringObject(FontType.CONSOLA);
    private final StringObject shipText = new StringObject(FontType.CONSOLA);

    public void update(int x, int y) {
        Core core = Core.get();
        Profiler profiler = core.getProfiler();
        float updateTime = profiler.getResult("update");
        float renderTime = profiler.getResult("render");
        int drawCalls = core.getRenderer().getLastFrameDrawCalls();
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

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemoryMB = maxMemory / 1024L / 1024L;
        long totalMemoryMB = totalMemory / 1024L / 1024L;
        long freeMemoryMB = freeMemory / 1024L / 1024L;

        int ups = MainServer.getInstance() != null ? MainServer.getInstance().getUps() : 0;
        int sectionOffset = 20;
        ParticleRenderer particleRenderer = core.getRenderer().getParticleRenderer();

        stringBuilder.setLength(0);
        stringBuilder.append("BFSR Client Dev 0.0.4 \n");
        stringBuilder.append("FPS ");
        stringBuilder.append(core.getRenderer().getFps());
        stringBuilder.append(", Local Server UPS ");
        stringBuilder.append(ups);
        stringBuilder.append(" \n");
        stringBuilder.append("Memory: ");
        stringBuilder.append(totalMemoryMB - freeMemoryMB);
        stringBuilder.append("MB / ");
        stringBuilder.append(totalMemoryMB);
        stringBuilder.append("MB up to ");
        stringBuilder.append(maxMemoryMB);
        stringBuilder.append("MB \n");
        stringBuilder.append("OpenGL: ");
        stringBuilder.append(openGlRenderer);
        stringBuilder.append(" \nVersion ");
        stringBuilder.append(openGlVersion);
        stringBuilder.append(" \n");
        stringBuilder.append(" \n");
        stringBuilder.append("Update: ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(updateTime));
        stringBuilder.append("ms / ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(sUpdateTime));
        stringBuilder.append("ms ");
        stringBuilder.append("\nPhysics: ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(physicsTime));
        stringBuilder.append("ms / ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(sPhysicsTime));
        stringBuilder.append("ms ");
        stringBuilder.append("\nRender: ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(renderTime));
        stringBuilder.append("ms ");
        stringBuilder.append(drawCalls);
        stringBuilder.append(" draw calls ");
        stringBuilder.append("\nParticle Renderer: ");
        stringBuilder.append(particleRenderer.getTaskCount() > 1 ? particleRenderer.getTaskCount() + " active threads" : "single-threaded");
        stringBuilder.append("\nNetwork: ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(netTime));
        stringBuilder.append("ms / ");
        stringBuilder.append(DecimalUtils.formatWithToDigits(sNetworkTime));
        stringBuilder.append("ms ");
        stringBuilder.append("\nPing: ");
        stringBuilder.append(ping);
        stringBuilder.append("ms");
        upperText.update(stringBuilder.toString());
        upperText.setPosition(x, y);

        y += upperText.getHeight() + sectionOffset;

        WorldClient world = core.getWorld();
        if (world != null) {
            Camera cam = core.getRenderer().getCamera();
            Vector2f camPos = cam.getPosition();
            int bulletsCount = world.getBullets().size();
            int shipsCount = world.getShips().size();
            int particlesCount = particleRenderer.getParticlesCount();
            int physicParticles = world.getParticleManager().getWreckCount();

            WorldServer sWorld = MainServer.getInstance() != null ? MainServer.getInstance().getWorld() : null;
            int sBulletsCount = sWorld != null ? sWorld.getBullets().size() : 0;
            int sShipsCount = sWorld != null ? sWorld.getShips().size() : 0;
            int sParticlesCount = sWorld != null ? sWorld.getParticles().size() : 0;
            stringBuilder.setLength(0);
            stringBuilder.append("---World--- ");
            stringBuilder.append("\nCamera pos: ");
            stringBuilder.append(DecimalUtils.formatWithToDigits(camPos.x));
            stringBuilder.append(", ");
            stringBuilder.append(DecimalUtils.formatWithToDigits(camPos.y));
            stringBuilder.append("\nShips count: ");
            stringBuilder.append(shipsCount);
            stringBuilder.append("/");
            stringBuilder.append(sShipsCount);
            stringBuilder.append(" \nBullets count: ");
            stringBuilder.append(bulletsCount);
            stringBuilder.append("/");
            stringBuilder.append(sBulletsCount);
            stringBuilder.append(" \nParticles count: ");
            stringBuilder.append(particlesCount);
            stringBuilder.append(" \nPhysic particles count: ");
            stringBuilder.append(physicParticles);
            stringBuilder.append("/");
            stringBuilder.append(sParticlesCount);
            worldText.update(stringBuilder.toString());
            worldText.setPosition(x, y);

            y += worldText.getHeight() + sectionOffset;
            Ship playerShip = world.getPlayerShip();
            if (playerShip != null) {
                Vector2f pos = playerShip.getPosition();
                Vector2f velocity = playerShip.getVelocity();
                Hull hull = playerShip.getHull();
                ShieldCommon shield = playerShip.getShield();
                Reactor reactor = playerShip.getReactor();
                stringBuilder.setLength(0);
                stringBuilder.append("---Player Ship--- ");
                stringBuilder.append("\nShip = ");
                stringBuilder.append(playerShip.getClass().getSimpleName());
                stringBuilder.append("\nPos: ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(pos.x));
                stringBuilder.append(", ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(pos.y));
                stringBuilder.append("\nVelocity: ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(velocity.x));
                stringBuilder.append(", ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(velocity.y));
                stringBuilder.append("\nMass: ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(playerShip.getBody().getMass().getMass()));
                stringBuilder.append("\nHull: ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(hull.getHull()));
                stringBuilder.append("/");
                stringBuilder.append(DecimalUtils.formatWithToDigits(hull.getMaxHull()));
                stringBuilder.append("\nShield: ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(shield.getShield()));
                stringBuilder.append("/");
                stringBuilder.append(DecimalUtils.formatWithToDigits(shield.getMaxShield()));
                stringBuilder.append("\nReactor: ");
                stringBuilder.append(DecimalUtils.formatWithToDigits(reactor.getEnergy()));
                stringBuilder.append("/");
                stringBuilder.append(DecimalUtils.formatWithToDigits(reactor.getMaxEnergy()));
                shipText.update(stringBuilder.toString());
                shipText.setPosition(x, y);
            }
        }
    }

    public void render() {
        upperText.render();
        worldText.render();

        if (Core.get().getWorld().getPlayerShip() != null) {
            shipText.render();
        }
    }
}
