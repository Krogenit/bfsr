package net.bfsr.world;

import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureGenerator;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.math.ModelMatrixType;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.server.EnumCommand;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.Random;

public class WorldClient extends World {
    private final Core core;
    private final TextureObject background = new TextureObject(TextureLoader.dummyTexture, 0, 0, 2560 << 1, 2560 << 1).setModelMatrixType(ModelMatrixType.BACKGROUND);
    private Ship playerShip;
    private int spawnTimer;
    private Texture backgroundTexture;
    private boolean disableLeftClickShipSelection;

    public WorldClient() {
        super(true, Core.getCore().getProfiler());

        this.core = Core.getCore();
    }

    public void setSeed(long seed) {
        if (backgroundTexture != null) backgroundTexture.delete();
        createBackground(seed);
    }

    private void createBackground(long seed) {
        backgroundTexture = TextureGenerator.generateNebulaTexture((int) background.getScale().x, (int) background.getScale().y, new Random(seed));
        background.setTexture(backgroundTexture);
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
                    if (ship.getAABB().isIntersects(Mouse.getWorldPosition(core.getRenderer().getCamera()))) {
                        core.getGuiInGame().selectShip(ship);
                    }
                }
            }
        }
    }

    public void onMouseLeftRelease() {
        DecimalFormat f = new DecimalFormat("0.00");
        Vector2f mpos = Mouse.getWorldPosition(core.getRenderer().getCamera());
        System.out.println("vertecies[0] = new Vector2(" + f.format(mpos.x) + "f, " + f.format(mpos.y) + "f);");
    }

    public void onMouseRightClicked() {
        core.getGuiInGame().selectShipSecondary(null);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            Ship ship = ships.get(i);
            if (ship.getAABB().isIntersects(Mouse.getWorldPosition(core.getRenderer().getCamera()))) {
                core.getGuiInGame().selectShipSecondary(ship);
            }
        }
    }

    public void input(int key) {
        int bots = 0;
        boolean sameFaction = true;
        Faction lastFaction = null;
        for (Ship s : ships) {
            if (s.isBot()) {
                bots++;
            }

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
            Vector2f pos = Mouse.getWorldPosition(Core.getCore().getRenderer().getCamera());

            if (core.getNetworkManager() != null)
//					for(int i=0;i<1;i++) {
//						Vector2f pos = new Vector2f(Core.getCore().getRenderer().getCamera().getPosition()).add(RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 5500 * rand.nextFloat()));
                core.sendPacket(new PacketCommand(EnumCommand.SpawnShip, "" + pos.x, "" + pos.y));
//					}
            spawnTimer = 60;
        } else if (key == GLFW.GLFW_KEY_G) {
            Vector2f pos = Mouse.getWorldPosition(Core.getCore().getRenderer().getCamera());
            Vector2f randomVector1 = new Vector2f(pos).add(-10 + rand.nextInt(21), -10 + rand.nextInt(21));
            core.sendPacket(new PacketCommand(EnumCommand.SpawnParticle, "" + randomVector1.x, "" + randomVector1.y));


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
            Core.getCore().setPaused(!Core.getCore().isPaused());
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_C) {
            Core.getCore().setCurrentGui(null);
        }
    }

    @Override
    protected void postPhysicsUpdate() {
        super.postPhysicsUpdate();
        Core.getCore().getRenderer().getParticleRenderer().postPhysicsUpdate();
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
    public void addShip(Ship ship) {
        super.addShip(ship);
    }

    @Override
    protected void removeShip(Ship ship, int index) {
        super.removeShip(ship, index);

        if (ship == playerShip)
            playerShip = null;
    }

    public void renderAmbient(float interpolation) {
        InstancedRenderer.INSTANCE.addToRenderPipeLine(background, interpolation);
        InstancedRenderer.INSTANCE.render();
    }

    public void renderEntities(BaseShader shader, float interpolation) {
        AxisAlignedBoundingBox cameraAABB = core.getRenderer().getCamera().getBoundingBox();

        int size = ships.size();
        for (int i = 0; i < size; i++) {
            Ship s = ships.get(i);
            if (s.getAABB().isIntersects(cameraAABB)) {
                s.render(shader, interpolation);
            }
        }

        InstancedRenderer.INSTANCE.render();
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        for (int i = 0; i < size; i++) {
            Ship s = ships.get(i);
            if (s.getAABB().isIntersects(cameraAABB)) {
                s.renderTransparent(shader, interpolation);
            }
        }

        size = bullets.size();
        for (int i = 0; i < size; i++) {
            Bullet b = bullets.get(i);
            if (b.getAABB().isIntersects(cameraAABB)) {
                b.render(shader, interpolation);
            }
        }

        InstancedRenderer.INSTANCE.render();
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void renderDebug(ShaderProgram shaderProgram) {
        core.getRenderer().getCamera().setupOpenGLMatrix();

        int size = ships.size();
        for (int i = 0; i < size; i++) {
            Ship s = ships.get(i);
            s.renderDebug();
        }

        size = bullets.size();
        for (int i = 0; i < size; i++) {
            Bullet bullet = bullets.get(i);
            bullet.renderDebug();
        }
    }

    public void setPlayerShip(Ship playerShip) {
        this.playerShip = playerShip;
        core.getGuiInGame().selectShip(playerShip);
        core.getGuiInGame().setShipControl();
    }

    public Ship getPlayerShip() {
        return playerShip;
    }

    public void disableShipDeselection() {
        disableLeftClickShipSelection = true;
    }
}
