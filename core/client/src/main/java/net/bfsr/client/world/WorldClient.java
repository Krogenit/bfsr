package net.bfsr.client.world;

import clipper2.core.PathD;
import clipper2.core.PointD;
import lombok.Getter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.entity.bullet.Bullet;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.entity.wreck.ShipWreckDamagable;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.network.packet.client.PacketCommand;
import net.bfsr.client.network.packet.client.PacketPauseGame;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.client.renderer.OpenGLHelper;
import net.bfsr.client.renderer.debug.DebugRenderer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureGenerator;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.settings.Option;
import net.bfsr.command.Command;
import net.bfsr.faction.Faction;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.DecimalUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

public class WorldClient extends World<Ship, Bullet> {
    private final Core core;
    private final TextureObject background = new TextureObject(2560 << 1, 2560 << 1);
    private Ship playerShip;
    private int spawnTimer;
    private Texture backgroundTexture = new Texture(0, 0).create();
    private boolean disableLeftClickShipSelection;
    @Getter
    private final ParticleManager particleManager = new ParticleManager();

    private final List<ShipWreckDamagable> shipWrecks = new ArrayList<>();
    private final Queue<ShipWreckDamagable> damagesToAdd = new LinkedList<>();

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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, TextureLoader.getTexture(TextureRegister.damageFire, GL_REPEAT, GL_LINEAR).getId());
    }

    public void onMouseLeftClicked() {
        debugClick();
        if (playerShip == null && core.canControlShip()) {
            if (disableLeftClickShipSelection) {
                disableLeftClickShipSelection = false;
            } else {
                core.getGuiInGame().selectShip(null);
                Vector2f mousePosition = Mouse.getWorldPosition(core.getRenderer().getCamera());
                int size = ships.size();
                for (int i = 0; i < size; i++) {
                    Ship ship = ships.get(i);
                    if (ship.getAabb().contains(mousePosition.x, mousePosition.y)) {
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
        Vector2f mousePosition = Mouse.getWorldPosition(core.getRenderer().getCamera());
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            Ship ship = ships.get(i);
            if (ship.getAabb().contains(mousePosition.x, mousePosition.y)) {
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

            if (core.getNetworkSystem() != null)
//					for(int i=0;i<1;i++) {
//						Vector2f pos = new Vector2f(Core.getCore().getRenderer().getCamera().getPosition()).add(RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 5500 * rand.nextFloat()));
                core.sendTCPPacket(new PacketCommand(Command.SPAWN_SHIP, "" + pos.x, "" + pos.y));
//					}
            spawnTimer = 60;
        } else if (key == GLFW.GLFW_KEY_G) {
            Vector2f pos = Mouse.getWorldPosition(Core.get().getRenderer().getCamera());
            Vector2f randomVector1 = new Vector2f(pos).add(-10 + rand.nextInt(21), -10 + rand.nextInt(21));
            core.sendTCPPacket(new PacketCommand(Command.SPAWN_PARTICLE, "" + randomVector1.x, "" + randomVector1.y));


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
            Core.get().sendTCPPacket(new PacketPauseGame());
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_C) {
            Core.get().setCurrentGui(null);
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_R) {
            Core.get().getRenderer().reloadShaders();
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_B) {
            Option.SHOW_DEBUG_BOXES.setValue(!Option.SHOW_DEBUG_BOXES.getBoolean());
        }
    }

    @Override
    protected void postPhysicsUpdate() {
        super.postPhysicsUpdate();
        particleManager.postPhysicsUpdate();
        for (int i = 0; i < shipWrecks.size(); i++) {
            shipWrecks.get(i).postPhysicsUpdate();
        }
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

        while (damagesToAdd.size() > 0) {
            ShipWreckDamagable shipWreckDamagable = damagesToAdd.poll();
            shipWrecks.add(shipWreckDamagable);
            addPhysicObject(shipWreckDamagable);
        }

        for (int i = 0; i < shipWrecks.size(); i++) {
            ShipWreckDamagable shipWreckDamagable = shipWrecks.get(i);
            shipWreckDamagable.update();
            if (shipWreckDamagable.isDead()) {
                shipWrecks.remove(i--);
                removePhysicObject(shipWreckDamagable);
            } else if (shipWreckDamagable.getFixturesToAdd().size() > 0) {
                shipWreckDamagable.getBody().removeAllFixtures();
                List<BodyFixture> fixturesToAdd = shipWreckDamagable.getFixturesToAdd();
                while (fixturesToAdd.size() > 0) {
                    shipWreckDamagable.getBody().addFixture(fixturesToAdd.remove(0));
                }

                shipWreckDamagable.getBody().updateMass();
            }
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
            AABB cameraAABB = core.getRenderer().getCamera().getBoundingBox();

            for (int i = 0, size = ships.size(); i < size; i++) {
                Ship s = ships.get(i);
                if (s.getAabb().overlaps(cameraAABB)) {
                    s.render();
                }
            }

            for (int i = 0, size = shipWrecks.size(); i < size; i++) {
                ShipWreckDamagable shipWreckDamagable = shipWrecks.get(i);
                if (shipWreckDamagable.getAabb().overlaps(cameraAABB)) {
                    shipWreckDamagable.render();
                }
            }

            particleManager.render();
        }, BufferType.ENTITIES_ALPHA);
        SpriteRenderer.INSTANCE.addTask(() -> {
            AABB cameraAABB = core.getRenderer().getCamera().getBoundingBox();

            for (int i = 0, size = ships.size(); i < size; i++) {
                Ship s = ships.get(i);
                if (s.getAabb().overlaps(cameraAABB)) {
                    s.renderAdditive();
                }
            }

            particleManager.renderAdditive();

            for (int i = 0, size = bullets.size(); i < size; i++) {
                Bullet b = bullets.get(i);
                if (b.getAabb().overlaps(cameraAABB)) {
                    b.render();
                }
            }
        }, BufferType.ENTITIES_ADDITIVE);
    }

//    private MaskShader maskShader = new MaskShader();
//    private DamageMaskTexture damageMaskTexture = new DamageMaskTexture(128, 128, BufferUtils.createByteBuffer(128 * 128));
//    private DamageMaskTexture backup = new DamageMaskTexture(128, 128, BufferUtils.createByteBuffer(128 * 128));

    {
//        maskShader.load();
//        maskShader.init();

        DamageMaskTexture damageMaskTexture = new DamageMaskTexture(128, 128, BufferUtils.createByteBuffer(128 * 128));
        damageMaskTexture.createWhiteMask();

        PathD pathD = new PathD();
        pathD.add(new PointD(-73.40f, -24.96f));
        pathD.add(new PointD(-70.86f, -24.96f));
        pathD.add(new PointD(-70.86f, -27.02f));
        pathD.add(new PointD(-69.80f, -26.96f));
        pathD.add(new PointD(-69.83f, -29.42f));
        pathD.add(new PointD(-51.30f, -29.39f));
        pathD.add(new PointD(-51.20f, -21.66f));
        pathD.add(new PointD(-61.16f, -21.72f));
        pathD.add(new PointD(-61.13f, -20.92f));
        pathD.add(new PointD(-56.16f, -20.92f));
        pathD.add(new PointD(-56.23f, -17.72f));
        pathD.add(new PointD(-55.16f, -16.86f));
        pathD.add(new PointD(-54.63f, -14.92f));
        pathD.add(new PointD(-52.76f, -14.99f));
        pathD.add(new PointD(-51.76f, -13.46f));
        pathD.add(new PointD(-50.16f, -13.42f));
        pathD.add(new PointD(-49.26f, -14.96f));
        pathD.add(new PointD(-46.23f, -14.96f));
        pathD.add(new PointD(-44.66f, -13.42f));
        pathD.add(new PointD(-44.66f, -9.86f));
        pathD.add(new PointD(-40.73f, -9.89f));
        pathD.add(new PointD(-34.63f, -4.96f));
        pathD.add(new PointD(-34.70f, 4.78f));
        pathD.add(new PointD(-40.70f, 9.71f));
        pathD.add(new PointD(-44.76f, 9.74f));
        pathD.add(new PointD(-44.70f, 13.24f));
        pathD.add(new PointD(-46.20f, 14.74f));
        pathD.add(new PointD(-49.36f, 14.74f));
        pathD.add(new PointD(-50.30f, 13.28f));
        pathD.add(new PointD(-51.80f, 13.18f));
        pathD.add(new PointD(-52.83f, 14.74f));
        pathD.add(new PointD(-54.26f, 14.71f));
        pathD.add(new PointD(-56.20f, 17.74f));
        pathD.add(new PointD(-56.30f, 20.31f));
        pathD.add(new PointD(-61.23f, 20.31f));
        pathD.add(new PointD(-61.26f, 21.64f));
        pathD.add(new PointD(-51.33f, 21.61f));
        pathD.add(new PointD(-51.30f, 29.21f));
        pathD.add(new PointD(-69.76f, 29.24f));
        pathD.add(new PointD(-69.73f, 26.71f));
        pathD.add(new PointD(-70.86f, 26.71f));
        pathD.add(new PointD(-70.96f, 24.81f));
        pathD.add(new PointD(-73.43f, 24.81f));
        pathD.add(new PointD(-73.40f, 16.34f));
        pathD.add(new PointD(-80.86f, 16.28f));
        pathD.add(new PointD(-80.83f, 13.81f));
        pathD.add(new PointD(-83.40f, 13.74f));
        pathD.add(new PointD(-83.40f, 8.28f));
        pathD.add(new PointD(-85.90f, 8.18f));
        pathD.add(new PointD(-85.93f, 1.51f));
        pathD.add(new PointD(-74.46f, 1.51f));
        pathD.add(new PointD(-74.43f, -1.69f));
        pathD.add(new PointD(-85.86f, -1.76f));
        pathD.add(new PointD(-85.93f, -8.49f));
        pathD.add(new PointD(-83.40f, -8.50f));
        pathD.add(new PointD(-83.40f, -14.04f));
        pathD.add(new PointD(-80.88f, -13.96f));
        pathD.add(new PointD(-80.88f, -16.49f));
        pathD.add(new PointD(-73.38f, -16.56f));

        float scaleX = 64.0f;
        float scaleY = 64.0f;
        float vertexScaleX = scaleX / 64.0f;
        float vertexScaleY = scaleY / 64.0f;
        try {
            for (int i = 0; i < pathD.size(); i++) {
                PointD vector2 = pathD.get(i);
                vector2.x += 60;
                vector2.x *= vertexScaleX;
                vector2.y *= vertexScaleY;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        float rotation = 0;
        double sin = Math.sin(rotation);
        double cos = Math.cos(rotation);

        try {
//            ShipDamagable shipDamagable = createDamage(-60, 0, sin, cos, scaleX, scaleY, pathD, damageMaskTexture);
//            addDamage(shipDamagable);

//            TextureUpdateRegion textureUpdateRegion = new TextureUpdateRegion();
//            Path64 path64 = new Path64(pathD.size());
//            for (int i = 0; i < pathD.size(); i++) {
//                path64.add(new Point64(pathD.get(i), DamageUtils.SCALE));
//            }
//            DamageUtils.clipTextureOutside(path64, damageMaskTexture, shipDamagable.getScale(), textureUpdateRegion);
//            textureUpdateRegion.upload(damageMaskTexture);
//            this.damageMaskTexture.createEmpty();
//            backup.copyFrom(shipDamagable.getMaskTexture().getByteBuffer());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addDamage(ShipWreckDamagable shipWreckDamagable) {
        damagesToAdd.add(shipWreckDamagable);
    }

    private void debugClick() {
//        Vector2f mouseWorldPos = Mouse.getWorldPosition(Core.get().getRenderer().getCamera());
//        ShipDamagable shipDamagable = shipDamagables.get(0);
//
//        Body body = shipDamagable.getBody();
//        double x = body.getTransform().getTranslationX();
//        double y = body.getTransform().getTranslationY();
//        double sin = body.getTransform().getSint();
//        double cos = body.getTransform().getCost();
//
//        float radius = 3.5f;
//        Path64 clip = DamageUtils.createCirclePath(mouseWorldPos.x - x, mouseWorldPos.y - y, -sin, cos, 12, radius);
//        float textureClipRadius = 8.0f;
//        DamageUtils.damage(shipDamagable, mouseWorldPos.x, mouseWorldPos.y, clip, textureClipRadius);
    }

    public void renderEntities() {
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        SpriteRenderer.INSTANCE.syncAndRender(BufferType.ENTITIES_ALPHA);
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        SpriteRenderer.INSTANCE.syncAndRender(BufferType.ENTITIES_ADDITIVE);
    }

    public void renderDebug() {
        core.getRenderer().getCamera().setupOpenGLMatrix();

//        try {
//            ShipDamagable shipDamagable1 = shipDamagables.get(1);
//            GL11.glPushMatrix();
//            GL11.glTranslatef((float) shipDamagable1.getBody().getTransform().getTranslationX(), (float) shipDamagable1.getBody().getTransform().getTranslationY(), 0.0f);
//            GL11.glScalef(0.5f, 0.5f, 1.0f);
//            GL11.glTranslatef(-shipDamagable1.getScale().x, -shipDamagable1.getScale().y, 0.0f);
//            backup.copyFrom(shipDamagable1.getMaskTexture().getByteBuffer());
//            long now = System.nanoTime();
//            TextureUpdateRegion textureUpdateRegion = new TextureUpdateRegion();
//            PathD pathD = shipDamagable1.getContours().get(0);
//            Path64 path64 = new Path64(pathD.size());
//            for (int i = 0; i < pathD.size(); i++) {
//                path64.add(new Point64(pathD.get(i), DamageUtils.SCALE));
//            }
//            DamageUtils.clipTextureOutside(path64, shipDamagable1.getMaskTexture(), shipDamagable1.getScale(), DamageUtils.SCALE, textureUpdateRegion);
//            textureUpdateRegion.upload(shipDamagable1.getMaskTexture());
//            shipDamagable1.getMaskTexture().copyFrom(backup.getByteBuffer());
//            GL11.glPopMatrix();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        for (int i = 0; i < shipWrecks.size(); i++) {
            DebugRenderer.INSTANCE.render(shipWrecks.get(i));
        }

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