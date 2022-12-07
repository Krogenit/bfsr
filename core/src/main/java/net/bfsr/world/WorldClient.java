package net.bfsr.world;

import net.bfsr.client.gui.ingame.GuiInGameMenu;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleRenderer;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.Program;
import net.bfsr.client.texture.Texture;
import net.bfsr.client.texture.TextureGenerator;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.math.EnumZoomFactor;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.server.EnumCommand;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.*;

public class WorldClient extends World {

    private final Core core;
    private TextureObject background;
    private final ParticleRenderer particleRenderer;
    private Ship playerShip;
    private int spawnTimer;
    private final HashMap<Texture, List<Ship>> shipsByMapForRender = new HashMap<>();
    private final HashMap<Texture, List<Bullet>> bulletByMapForRender = new HashMap<>();

    public WorldClient() {
        super(true, Core.getCore().getProfiler());

        this.core = Core.getCore();
        this.particleRenderer = new ParticleRenderer(this);
    }

    public void setSeed(long seed) {
        if (background != null) background.clear();
        createBackground(seed);
    }

    private void createBackground(long seed) {
        int width = 2560 * 2;
        int height = 2560 * 2;
        this.background = new TextureObject(TextureGenerator.generateNebulaTexture(width, height, new Random(seed)), new Vector2f(0, 0), new Vector2f(width, height));
        this.background.setZoomFactor(EnumZoomFactor.Background);
    }

    public void onMouseLeftClicked() {
        if (playerShip == null && core.canControlShip()) {
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

        if (key == GLFW.GLFW_KEY_ESCAPE && Core.getCore().canControlShip()) {
            Core.getCore().setCurrentGui(new GuiInGameMenu());
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

        }
    }

    @Override
    public void update() {
        super.update();
        particleRenderer.update();

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
    protected void removeShip(Ship ship) {
        super.removeShip(ship);

        if (ship == playerShip)
            playerShip = null;
    }

    public void renderAmbient(BaseShader shader) {
        if (background != null) {
            background.render(shader);
        }
    }

    public void renderEntities(BaseShader shader) {
        shader.enable();
        AxisAlignedBoundingBox cameraAABB = core.getRenderer().getCamera().getBoundingBox();

        Iterator<Texture> it = shipsByMapForRender.keySet().iterator();
        while (it.hasNext()) {
            Texture key = it.next();
            List<Ship> ss = shipsByMapForRender.get(key);
            ss.clear();
        }

        int size = ships.size();
        for (int i = 0; i < size; i++) {
            Ship s = ships.get(i);
            if (s.getAABB().isIntersects(cameraAABB)) {
                Texture t = s.getTexture();
                List<Ship> ss = shipsByMapForRender.computeIfAbsent(t, k -> new ArrayList<>());
                ss.add(s);
            }
        }

        it = shipsByMapForRender.keySet().iterator();
        while (it.hasNext()) {
            Texture key = it.next();
            List<Ship> ss = shipsByMapForRender.get(key);
            size = ss.size();
            for (int i = 0; i < size; i++) {
                Ship s = ss.get(i);
                s.render(shader);
            }
        }

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        it = bulletByMapForRender.keySet().iterator();
        while (it.hasNext()) {
            Texture key = it.next();
            List<Bullet> ss = bulletByMapForRender.get(key);
            ss.clear();
        }

        size = bullets.size();
        for (int i = 0; i < size; i++) {
            Bullet b = bullets.get(i);
            if (b.getAABB().isIntersects(cameraAABB)) {
                Texture t = b.getTexture();
                List<Bullet> ss = bulletByMapForRender.computeIfAbsent(t, k -> new ArrayList<>(1));
                ss.add(b);
            }
        }

        it = bulletByMapForRender.keySet().iterator();
        while (it.hasNext()) {
            Texture key = it.next();
            List<Bullet> ss = bulletByMapForRender.get(key);
            size = ss.size();
            for (int i = 0; i < size; i++) {
                Bullet b = ss.get(i);
                b.render(shader);
            }
        }

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void renderBackParticles() {
        particleRenderer.render(EnumParticlePositionType.Background);
    }

    public void renderParticles() {
        particleRenderer.render(EnumParticlePositionType.Default);
    }

    public void renderDebug(Program shaderProgram) {
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

    @Override
    public ParticleRenderer getParticleRenderer() {
        return particleRenderer;
    }

    public void setPlayerShip(Ship playerShip) {
        this.playerShip = playerShip;
        core.getGuiInGame().selectShip(playerShip);
        core.getGuiInGame().setShipControl();
    }

    public Ship getPlayerShip() {
        return playerShip;
    }

    @Override
    public void clear() {
        super.clear();
    }
}
