package net.bfsr.editor.hud;

import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.physics.CollisionHandler;
import net.bfsr.client.renderer.entity.BulletRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.world.entity.ClientEntityIdManager;
import net.bfsr.command.Command;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.particle.GuiParticleEditor;
import net.bfsr.editor.gui.ship.GuiShipEditor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;
import net.bfsr.engine.world.entity.PositionHistory;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.engine.world.entity.TransformData;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.physics.collision.CollisionMatrix;
import net.bfsr.server.entity.EntityManager;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_BUTTON_HEIGHT;
import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_STRING_OFFSET_X;
import static net.bfsr.editor.gui.EditorTheme.FONT;
import static net.bfsr.editor.gui.EditorTheme.FONT_SIZE;
import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;

public class EditorHUD extends HUD {
    private final Rectangle commandsRectangle = new Rectangle(100, 100);
    private boolean fastForwardTesting;
    private Ship fastForwardShip;
    private ShipRender shipRender;
    private final Vector2f shipVelocity = new Vector2f(0.01f, 0.01f);
    private final PositionHistory positionHistory = new PositionHistory(5000L);
    private World world;
    private Bullet bullet;
    private final Vector2f maxPosition = new Vector2f(4, 4);
    private BulletRender bulletRender;
    private Rectangle bulletAABB;
    private boolean bulletAdded;
    private boolean update = true;

    public EditorHUD() {
        int buttonWidth = 240;
        int buttonHeight = 36;
        int y = 0;
        add(EditorTheme.setupButton(new Button(buttonWidth, buttonHeight, "Particle Editor", 22,
                (mouseX, mouseY) -> Client.get().openGui(new GuiParticleEditor()))).atLeft(0, y));
        y += buttonHeight;
        add(EditorTheme.setupButton(new Button(buttonWidth, buttonHeight, "Ship Editor", 22,
                (mouseX, mouseY) -> Client.get().openGui(new GuiShipEditor()))).atLeft(0, y));
        y += buttonHeight;
        add(EditorTheme.setupButton(new Button(buttonWidth, buttonHeight, "Fast Forward Test", 22,
                (mouseX, mouseY) -> {
                    fastForwardTesting = !fastForwardTesting;

                    if (fastForwardTesting) {
                        world = new World(new Profiler(), 0L, new EventBus(), new EntityManager(), new ClientEntityIdManager(),
                                Client.get(), new CollisionMatrix(new CollisionHandler(Client.get())));
                        world.init();

                        Client client = Client.get();
                        ShipRegistry shipRegistry = client.getConfigConverterManager().getConverter(ShipRegistry.class);
                        fastForwardShip = new Ship(shipRegistry.get(3));
                        fastForwardShip.init(world, 0);
                        fastForwardShip.setFaction(Faction.HUMAN);
                        fastForwardShip.setSpawned();
                        client.getShipOutfitter().outfit(fastForwardShip);
                        world.add(fastForwardShip);

                        shipRender = new ShipRender(fastForwardShip);
                        shipRender.init();
                        client.getEntityRenderer().addRender(shipRender);

                        bullet = createBullet();
                    } else {
                        fastForwardShip.setDead();
                        bullet.setDead();
                        world.clear();
                    }
                })).atLeft(0, y));

        commandsRectangle.atTopRight(-otherShipOverlay.getWidth(), -20);
        commandsRectangle.setAllColors(0.2f, 0.2f, 0.2f, 0.75f);
        addShipCommandButtons(commandsRectangle);
    }

    private void addShipCommandButtons(GuiObject guiObject) {
        Command[] commands = Command.values();
        int y = 0;
        for (int i = 0; i < commands.length; i++) {
            Command command = commands[i];
            if (command.isShipCommand()) {
                String title = command.name().toLowerCase(Locale.getDefault()).replace("_", " ");
                Button destroyShipButton = new Button(guiObject.getWidth(), CONTEXT_MENU_BUTTON_HEIGHT, title, FONT, FONT_SIZE,
                        CONTEXT_MENU_STRING_OFFSET_X / 2, 0, StringOffsetType.DEFAULT,
                        (mouseX1, mouseY1) -> {
                            Ship ship = otherShipOverlay.getShip();
                            if (ship != null) {
                                Client.get().sendTCPPacket(command.createShipPacketCommand(command, ship));
                            }
                        });
                guiObject.add(setupContextMenuButton(destroyShipButton).atTopLeft(0, y));
                y -= CONTEXT_MENU_BUTTON_HEIGHT;
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        if (fastForwardTesting && !Client.get().isPaused() && update) {
            float x = fastForwardShip.getX();
            float y = fastForwardShip.getY();

            double renderTime = Client.get().getRenderTime();
            positionHistory.addPositionData(x, y, fastForwardShip.getSin(), fastForwardShip.getCos(), renderTime);

            fastForwardShip.setPosition(x + shipVelocity.x, y + shipVelocity.y);

            if (fastForwardShip.getX() > maxPosition.x * 0.4f) {
                if (!bulletAdded) {
                    bulletAdded = true;
                    bullet.getBody().setActive(true);
                    world.add(bullet);

                    float additionalSeconds = 0.5f;
                    float fastForwardTimeInNanos = (float) (Client.get().getClientRenderDelay() +
                            additionalSeconds * 1_000_000_000.0f);

                    float updateDeltaTime = Engine.getUpdateDeltaTime();
                    float updateDeltaTimeInMills = updateDeltaTime * 1000.0f;
                    float updateDeltaTimeInNanos = updateDeltaTimeInMills * 1_000_000.0f;
                    int iterations = Math.round(fastForwardTimeInNanos / 1_000_000.0f / updateDeltaTimeInMills);

                    float bulletX = bullet.getX();
                    float bulletY = bullet.getY();
                    Vector2 linearVelocity = bullet.getLinearVelocity();
                    float offset = 1.0f + Math.max(bullet.getSizeX(), bullet.getSizeY());
                    float endX = bulletX + linearVelocity.x * updateDeltaTime * iterations;
                    float endY = bulletY + linearVelocity.y * updateDeltaTime * iterations;
                    float minX = Math.min(bulletX, endX) - offset;
                    float minY = Math.min(bulletY, endY) - offset;
                    float maxX = Math.max(bulletX, endX) + offset;
                    float maxY = Math.max(bulletY, endY) + offset;
                    AbstractCamera camera = Client.get().getCamera();
                    float zoom = camera.getZoom();

                    float guiX = (minX - camera.getPosition().x) * zoom - camera.getOrigin().x;
                    float guiY = (minY - camera.getPosition().y) * zoom - camera.getOrigin().y;
                    float width = (maxX - camera.getPosition().x) * zoom - camera.getOrigin().x - guiX;
                    float height = (maxY - camera.getPosition().y) * zoom - camera.getOrigin().y - guiY;

                    bulletAABB = new Rectangle((int) width, (int) height);
                    bulletAABB.setAllColors(1, 0, 0, 0.5f);
                    add(bulletAABB.atBottomLeft((int) guiX, (int) guiY));

                    AABB aabb = new AABB(new Vector2(minX, minY), new Vector2(maxX, maxY));

                    Set<Body> affectedBodies = new HashSet<>();

                    world.getPhysicWorld().queryAABB(fixture -> {
                        Body body = fixture.getBody();
                        if (affectedBodies.contains(body)) {
                            return true;
                        }

                        if (body == bullet.getBody()) {
                            return true;
                        }

                        affectedBodies.add(body);
                        return true;
                    }, aabb);

                    if (iterations > 0) {
                        EntityDataHistoryManager entityDataHistoryManager = new EntityDataHistoryManager();

                        for (Body body : affectedBodies) {
                            RigidBody rigidBody = (RigidBody) body.getUserData();
                            entityDataHistoryManager.addPositionData(rigidBody.getId(), rigidBody.getX(), rigidBody.getY(),
                                    rigidBody.getSin(), rigidBody.getCos(), 0);
                        }

                        update = false;

                        world.getPhysicWorld().beginFastForward();

                        fastForwardIteration(0, iterations, affectedBodies, renderTime, fastForwardTimeInNanos,
                                updateDeltaTimeInNanos, entityDataHistoryManager);
                        return;
                    }
                }

                if (fastForwardShip.getX() > maxPosition.x) {
                    bulletAdded = false;
                    fastForwardShip.setPosition(0.0f, 0.0f);
                    bullet = createBullet();
                    remove(bulletAABB);
                }
            }

            world.update(renderTime);
        }
    }

    private void fastForwardIteration(int i, int max, Set<Body> affectedBodies, double renderTime, double fastForwardTimeInNanos,
                                      float updateDeltaTimeInNanos, EntityDataHistoryManager entityDataHistoryManager) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Client.get().addFutureTask(() -> {
                EntityDataHistoryManager dataHistoryManager = world.getEntityManager().getDataHistoryManager();

                for (Body body : affectedBodies) {
                    RigidBody rigidBody = (RigidBody) body.getUserData();
                    TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(),
                            renderTime - fastForwardTimeInNanos);
                    if (transformData != null) {
                        Vector2f position = transformData.getPosition();
                        body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
                    }
                }

                bullet.update();
                world.getPhysicWorld().fastForwardStep(Engine.getUpdateDeltaTime(), Collections.singletonList(bullet.getBody()));
                bullet.postPhysicsUpdate();
                fastForwardShip.postPhysicsUpdate();

                if (i + 1 == max) {
                    remove(bulletAABB);
                    world.getPhysicWorld().endFastForward();
                    update = true;

                    for (Body body : affectedBodies) {
                        RigidBody rigidBody = (RigidBody) body.getUserData();
                        TransformData transformData = entityDataHistoryManager.getFirstTransformData(rigidBody.getId());
                        Vector2f position = transformData.getPosition();
                        body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
                    }
                } else {
                    fastForwardIteration(i + 1, max, affectedBodies, renderTime, fastForwardTimeInNanos - updateDeltaTimeInNanos,
                            updateDeltaTimeInNanos, entityDataHistoryManager);
                }
            });
        });
        thread.start();
    }

    private Bullet createBullet() {
        Client client = Client.get();
        GunRegistry gunRegistry = client.getConfigConverterManager().getConverter(GunRegistry.class);
        GunData gunData = gunRegistry.get(0);
        float angle = MathUtils.HALF_PI * 0.5f;
        bullet = new Bullet(-0.5f, -0.5f, LUT.sin(angle), LUT.cos(angle), gunData, null,
                gunData.getDamage().copy());
        bullet.init(world, 1);
        bullet.getBody().setActive(false);

        bulletRender = new BulletRender(bullet);
        bulletRender.init();
        client.getEntityRenderer().addRender(bulletRender);

        return bullet;
    }

    @Override
    public void selectShipSecondary(Ship ship) {
        super.selectShipSecondary(ship);

        if (ship != null) {
            addIfAbsent(commandsRectangle);
        } else {
            remove(commandsRectangle);
        }
    }

    @Override
    public void remove() {
        super.remove();

        if (fastForwardShip != null) {
            fastForwardShip.setDead();
            bullet.setDead();
            world.clear();
        }
    }
}