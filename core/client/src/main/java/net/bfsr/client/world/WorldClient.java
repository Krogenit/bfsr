package net.bfsr.client.world;

import lombok.Getter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.entity.bullet.Bullet;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.network.packet.client.PacketCommand;
import net.bfsr.client.network.packet.client.PacketPauseGame;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.client.renderer.OpenGLHelper;
import net.bfsr.client.renderer.debug.DebugRenderer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureGenerator;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.command.Command;
import net.bfsr.faction.Faction;
import net.bfsr.util.DecimalUtils;
import net.bfsr.world.World;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class WorldClient extends World<Ship, Bullet> {
    private final Core core;
    private final TextureObject background = new TextureObject(2560 << 1, 2560 << 1);
    private Ship playerShip;
    private int spawnTimer;
    private Texture backgroundTexture = new Texture(0, 0);
    private boolean disableLeftClickShipSelection;
    @Getter
    private final ParticleManager particleManager = new ParticleManager();

    public WorldClient() {
        super(Core.get().getProfiler());

        this.core = Core.get();
    }

    public void setSeed(long seed) {
        createBackground(seed);
    }

    private void createBackground(long seed) {
        if (backgroundTexture != null) backgroundTexture.delete();
        backgroundTexture = TextureGenerator.generateNebulaTexture((int) background.getScale().x, (int) background.getScale().y, new Random(seed));
    }

    public void onMouseLeftClicked() {
        if (playerShip == null && core.canControlShip()) {
            if (disableLeftClickShipSelection) {
                disableLeftClickShipSelection = false;
            } else {
                core.getGuiInGame().selectShip(null);
                int size = ships.size();
                for (int i = 0; i < size; i++) {
                    Ship ship = ships.get(i);
                    if (ship.getWorldAABB().isIntersects(Mouse.getWorldPosition(core.getRenderer().getCamera()))) {
                        core.getGuiInGame().selectShip(ship);
                    }
                }
            }
        }
    }

    public void onMouseLeftRelease() {
        Vector2f mpos = Mouse.getWorldPosition(core.getRenderer().getCamera());
        System.out.println("vertecies[0] = new Vector2(" + DecimalUtils.formatWithToDigits(mpos.x) + "f, " + DecimalUtils.formatWithToDigits(mpos.y) + "f);");
    }

    public void onMouseRightClicked() {
        core.getGuiInGame().selectShipSecondary(null);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            Ship ship = ships.get(i);
            if (ship.getWorldAABB().isIntersects(Mouse.getWorldPosition(core.getRenderer().getCamera()))) {
                core.getGuiInGame().selectShipSecondary(ship);
            }
        }
    }

    public void input(int key) {
        int bots = 0;
        boolean sameFaction = true;
        Faction lastFaction = null;
        for (Ship s : ships) {
//            if (s.isBot()) {
//                bots++;
//            }

            if (lastFaction != null && lastFaction != s.getFaction()) {
                sameFaction = false;
            }

            lastFaction = s.getFaction();
        }

        //		if(Core.getCore().canControlShip()) {
        if (key == GLFW.GLFW_KEY_F
//					|| --spawnTimer <= 0
//					|| ((bots == 0 || sameFaction) && --spawnTimer <= 0)
        ) {
            Vector2f pos = Mouse.getWorldPosition(Core.get().getRenderer().getCamera());

            if (core.getNetworkManager() != null)
//					for(int i=0;i<1;i++) {
//						Vector2f pos = new Vector2f(Core.getCore().getRenderer().getCamera().getPosition()).add(RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 5500 * rand.nextFloat()));
                core.sendPacket(new PacketCommand(Command.SPAWN_SHIP, "" + pos.x, "" + pos.y));
//					}
            spawnTimer = 60;
        } else if (key == GLFW.GLFW_KEY_G) {
            Vector2f pos = Mouse.getWorldPosition(Core.get().getRenderer().getCamera());
            Vector2f randomVector1 = new Vector2f(pos).add(-10 + rand.nextInt(21), -10 + rand.nextInt(21));
            core.sendPacket(new PacketCommand(Command.SPAWN_PARTICLE, "" + randomVector1.x, "" + randomVector1.y));


//				particleSystem.spawnMediumGarbage(rand.nextInt(2) + 1, randomVector1, new Vector2f(),  50f + rand.nextFloat() * 40f);
//				particleSystem.spawnSmallGarbage(4, randomVector1, new Vector2f(), 50f);
//				particleSystem.spawnDamageDerbis(1, new Vector2f(), randomVector1);
//				particleSystem.spawnShipOst(randomVector1, new Vector2f());
//				particleSystem.spawnLight(randomVector1, 5f, new Vector4f(1.0f, 0.5f, 0.5f, 0.7f), 0.04f, false, EnumParticlePositionType.Default);
//				particleSystem.spawnSpark(randomVector1, 0.5f);
//				particleSystem.spawnExplosion(randomVector1, 0.125F);
        } else if (key == GLFW.GLFW_KEY_J) {
            if (playerShip != null) {
//				playerShip.getDamages().clear();
                //saimon
//				playerShip.addDamage(new Damage(playerShip, 0.2f, 0, new Vector2f(10, -4), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.4f, 0, new Vector2f(5, -18), 1f));
//				playerShip.addDamage(new Damage(playerShip, 0.6f, 1, new Vector2f(5, 15), 0.55f));
//				playerShip.addDamage(new Damage(playerShip, 0.8f, 3, new Vector2f(-19, 0), 0.6f));
                //engi
//				playerShip.addDamage(new Damage(playerShip, 0.2f, 0, new Vector2f(-10, 15), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.4f, 0, new Vector2f(5, -12), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.6f, 1, new Vector2f(-15, -0), 0.55f));
//				playerShip.addDamage(new Damage(playerShip, 0.8f, 2, new Vector2f(12, 5), 0.5f));
                //human
//				playerShip.addDamage(new Damage(playerShip, 0.2f, 0, new Vector2f(-5, 15), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.4f, 0, new Vector2f(-18, -8), 0.8f));
//				playerShip.addDamage(new Damage(playerShip, 0.6f, 1, new Vector2f(-5, -15), 0.55f));
//				playerShip.addDamage(new Damage(playerShip, 0.8f, 2, new Vector2f(8, -2), 0.5f));
            }

        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_P) {
            Core.get().setPaused(!Core.get().isPaused());
            Core.get().sendPacket(new PacketPauseGame());
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_C) {
            Core.get().setCurrentGui(null);
        }
    }

    @Override
    protected void postPhysicsUpdate() {
        super.postPhysicsUpdate();
        particleManager.postPhysicsUpdate();
    }

    @Override
    protected void updateParticles() {
        particleManager.update();
    }

    @Override
    protected void updateShips() {
        super.updateShips();
        if (playerShip != null) {
            if (core.canControlShip() && playerShip.isSpawned())
                playerShip.control();
        }
    }

    @Override
    protected void removeShip(Ship ship, int index) {
        super.removeShip(ship, index);

        if (ship == playerShip)
            playerShip = null;
    }

    public void prepareAmbient() {
        Vector2f scale = background.getScale();
        float moveFactor = 0.005f;
        Camera camera = Core.get().getRenderer().getCamera();
        float cameraZoom = camera.getLastZoom() + (camera.getZoom() - camera.getLastZoom()) * Core.get().getRenderer().getInterpolation();
        float lastX = (camera.getLastPosition().x - camera.getLastPosition().x * moveFactor / cameraZoom);
        float lastY = (camera.getLastPosition().y - camera.getLastPosition().y * moveFactor / cameraZoom);
        float x = (camera.getPosition().x - camera.getPosition().x * moveFactor / cameraZoom);
        float y = (camera.getPosition().y - camera.getPosition().y * moveFactor / cameraZoom);
        float zoom = (float) (0.5f + Math.log(cameraZoom) * 0.01f);
        float scaleX = scale.x / cameraZoom * zoom;
        float scaleY = scale.y / cameraZoom * zoom;
        SpriteRenderer.INSTANCE.addToRenderPipeLine(lastX, lastY, x, y, 0, 0, scaleX, scaleY, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f, backgroundTexture, BufferType.BACKGROUND);
    }

    public void renderAmbient() {
        SpriteRenderer.INSTANCE.render(BufferType.BACKGROUND);
    }

    public void prepareEntities() {
        SpriteRenderer.INSTANCE.addTask(() -> {
            AxisAlignedBoundingBox cameraAABB = core.getRenderer().getCamera().getBoundingBox();

            for (int i = 0, size = ships.size(); i < size; i++) {
                Ship s = ships.get(i);
                if (s.getWorldAABB().isIntersects(cameraAABB)) {
                    s.render();
                }
            }

            particleManager.render();
        }, BufferType.ENTITIES_ALPHA);
        SpriteRenderer.INSTANCE.addTask(() -> {
            AxisAlignedBoundingBox cameraAABB = core.getRenderer().getCamera().getBoundingBox();

            for (int i = 0, size = ships.size(); i < size; i++) {
                Ship s = ships.get(i);
                if (s.getWorldAABB().isIntersects(cameraAABB)) {
                    s.renderAdditive();
                }
            }

            particleManager.renderAdditive();

            for (int i = 0, size = bullets.size(); i < size; i++) {
                Bullet b = bullets.get(i);
                if (b.getWorldAABB().isIntersects(cameraAABB)) {
                    b.render();
                }
            }
        }, BufferType.ENTITIES_ADDITIVE);
    }

    public void renderEntities() {
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        SpriteRenderer.INSTANCE.syncAndRender(BufferType.ENTITIES_ALPHA);
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        SpriteRenderer.INSTANCE.syncAndRender(BufferType.ENTITIES_ADDITIVE);
    }

    public void renderDebug() {
        core.getRenderer().getCamera().setupOpenGLMatrix();

        for (int i = 0, size = ships.size(); i < size; i++) {
            DebugRenderer.INSTANCE.render(ships.get(i));
        }

        for (int i = 0, size = bullets.size(); i < size; i++) {
            Bullet bullet = bullets.get(i);
            DebugRenderer.INSTANCE.render(bullet);
        }

        particleManager.renderDebug();
    }

    public void setPlayerShip(Ship playerShip) {
        this.playerShip = playerShip;
        core.getGuiInGame().selectShip(playerShip);
        core.getGuiInGame().onShipControlStarted();
    }

    public Ship getPlayerShip() {
        return playerShip;
    }

    public void disableShipDeselection() {
        disableLeftClickShipSelection = true;
    }

    @Override
    public void clear() {
        super.clear();
        particleManager.clear();
    }
}
