package net.bfsr.client.renderer.entity;

import gnu.trove.map.TMap;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.EngineEffects;
import net.bfsr.client.renderer.component.ModuleRenderer;
import net.bfsr.client.renderer.component.WeaponRenderRegistry;
import net.bfsr.client.renderer.component.WeaponSlotRender;
import net.bfsr.config.entity.ship.EngineData;
import net.bfsr.config.entity.ship.EnginesData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.listener.OneTimeEventListener;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class ShipRender extends DamageableRigidBodyRenderer<Ship> {
    private static final AbstractTexture JUMP_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.particleJump);
    private static final AbstractTexture REACTOR_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.moduleReactor);
    private static final AbstractTexture ENGINE_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.moduleEngine);
    private static final AbstractTexture SHIELD_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.moduleShield);

    private final StringObject stringObject = new StringObject(FontType.XOLONIUM, 14, StringOffsetType.CENTERED);

    private final List<WeaponSlotRender<? extends WeaponSlot>> weaponRenders = new ArrayList<>();
    private final AbstractTexture shieldTexture;

    private final Vector2f jumpPosition = new Vector2f();
    private final Vector2f lastJumpPosition = new Vector2f();
    private float lastJumpDelta;
    private float jumpDelta;

    private final EnumMap<Direction, List<SpawnAccumulator>> engineAccumulators = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, Runnable> engineEffectsRunnable = new EnumMap<>(Direction.class);
    private final List<ModuleRenderer> moduleRenders = new ArrayList<>();
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final Vector2f rotateToVector = new Vector2f();

    public ShipRender(Ship ship) {
        super(Engine.assetsManager.getTexture(ship.getConfigData().getTexture()), ship);

        shieldTexture = Engine.assetsManager.getTexture(ship.getModules().getShield().getShieldData().getTexturePath());
        createWeaponSlotsRenders(ship);
        initEngineEffectsRunnable(ship);
        createModuleRenders(ship);
    }

    private void createWeaponSlotsRenders(Ship ship) {
        Modules modules = ship.getModules();
        List<WeaponSlot> weaponSlots = modules.getWeaponSlots();
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            weaponRenders.add(WeaponRenderRegistry.createRender(weaponSlot));
        }
    }

    private void initEngineEffectsRunnable(Ship ship) {
        TMap<Direction, EnginesData> engines = ship.getConfigData().getEngines();
        engines.forEachEntry((direction, enginesData) -> {
            int size = enginesData.engines().size();
            ArrayList<SpawnAccumulator> accumulators = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                accumulators.add(new SpawnAccumulator());
            }
            engineAccumulators.put(direction, accumulators);
            return true;
        });

        Body body = ship.getBody();
        Vector2f shipPos = ship.getPosition();
        Vector4f effectsColor = ship.getConfigData().getEffectsColor();
        Vector2 shipVelocity = body.getLinearVelocity();

        Direction[] directions = Direction.VALUES;
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            Runnable runnable;
            if (direction == Direction.STOP) {
                runnable = () -> {
                    float x = -(float) body.getLinearVelocity().x;
                    float y = -(float) body.getLinearVelocity().y;

                    if (Math.abs(x) > 0.5f) {
                        engineEffectsRunnable.get(rigidBodyUtils.calculateDirectionToPoint(ship, x + shipPos.x, shipPos.y)).run();
                    }

                    if (Math.abs(y) > 0.5f) {
                        engineEffectsRunnable.get(rigidBodyUtils.calculateDirectionToPoint(ship, shipPos.x, y + shipPos.y)).run();
                    }
                };
            } else {
                EnginesData enginesData = engines.get(direction);
                if (enginesData != null) {
                    List<net.bfsr.entity.ship.module.engine.Engine> engineModules = ship.getModules().getEngines()
                            .getEngines(direction);
                    List<SpawnAccumulator> accumulators = engineAccumulators.get(direction);
                    List<EngineData> engineDataList = enginesData.engines();

                    if (direction == Direction.FORWARD) {
                        runnable = () -> {
                            float sin = ship.getSin();
                            float cos = ship.getCos();

                            for (int j = 0; j < engineDataList.size(); j++) {
                                if (!engineModules.get(j).isDead()) {
                                    Vector2f effectPosition = engineDataList.get(j).effectPosition();
                                    RotationHelper.rotate(sin, cos, effectPosition.x, effectPosition.y, rotateToVector);
                                    EngineEffects.smallEngine(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y, sin,
                                            cos, 10.0f, (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f,
                                            effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, accumulators.get(j));
                                }
                            }
                        };
                    } else {
                        runnable = () -> {
                            float sin = ship.getSin();
                            float cos = ship.getCos();

                            for (int j = 0; j < engineDataList.size(); j++) {
                                if (!engineModules.get(j).isDead()) {
                                    Vector2f effectPosition = engineDataList.get(j).effectPosition();
                                    RotationHelper.rotate(sin, cos, effectPosition.x, effectPosition.y, rotateToVector);
                                    EngineEffects.secondaryEngine(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y,
                                            accumulators.get(j));
                                }
                            }
                        };
                    }
                } else {
                    runnable = RunnableUtils.EMPTY_RUNNABLE;
                }
            }

            engineEffectsRunnable.put(direction, runnable);
        }
    }

    private void createModuleRenders(Ship ship) {
        Modules modules = object.getModules();

        moduleRenders.add(new ModuleRenderer(ship, modules.getReactor(), REACTOR_TEXTURE));

        Shield shield = modules.getShield();
        if (!shield.isDead()) {
            ModuleRenderer moduleRenderer = new ModuleRenderer(ship, shield, SHIELD_TEXTURE);
            moduleRenders.add(moduleRenderer);
            shield.addDestroyListener(new OneTimeEventListener(shield, () -> moduleRenders.remove(moduleRenderer)));
        }

        Engines enginesModule = modules.getEngines();
        TMap<Direction, EnginesData> engines = object.getConfigData().getEngines();
        engines.forEachEntry((direction, enginesData) -> {
            List<EngineData> engineDataList = enginesData.engines();
            for (int i = 0; i < engineDataList.size(); i++) {
                net.bfsr.entity.ship.module.engine.Engine engine = enginesModule.getEngines(direction).get(i);
                if (!engine.isDead()) {
                    ModuleRenderer moduleRenderer = new ModuleRenderer(ship, engine, ENGINE_TEXTURE, direction);
                    moduleRenders.add(moduleRenderer);
                    engine.addDestroyListener(new OneTimeEventListener(shield, () -> moduleRenders.remove(moduleRenderer)));
                }
            }

            return true;
        });
    }

    @Override
    public void update() {
        super.update();

        if (!object.isSpawned()) {
            lastJumpDelta = jumpDelta;
            lastJumpPosition.set(jumpPosition);
        }

        for (int i = 0; i < weaponRenders.size(); i++) {
            weaponRenders.get(i).update();
        }

        for (int i = 0; i < moduleRenders.size(); i++) {
            moduleRenders.get(i).update();
        }

        object.getMoveDirections().forEach(direction -> {
            engineEffectsRunnable.get(direction).run();
            return true;
        });
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();

        for (int i = 0; i < weaponRenders.size(); i++) {
            weaponRenders.get(i).postWorldUpdate();
        }

        for (int i = 0; i < moduleRenders.size(); i++) {
            moduleRenders.get(i).postWorldUpdate();
        }

        if (!object.isSpawned()) {
            Vector2f position = object.getPosition();
            Vector2f objectJumpPosition = object.getJumpPosition();
            jumpDelta = 1.0f - object.getJumpTimer() / (float) object.getJumpTimeInTicks();
            jumpPosition.set(objectJumpPosition.x + (position.x - objectJumpPosition.x) * jumpDelta * 0.9f,
                    objectJumpPosition.y + (position.y - objectJumpPosition.y) * jumpDelta * 0.9f);
        }
    }

    @Override
    protected void updateAABB() {
        super.updateAABB();

        Vector2f position = object.getPosition();
        float halfStringWidth = stringObject.getWidth() / (stringObject.getFontSize() * 1.4f);
        aabb.union(position.x - halfStringWidth, aabb.getMinY(), position.x + halfStringWidth,
                position.y + 3.2f + object.getSize().y / 4.0f);

        for (int i = 0; i < weaponRenders.size(); i++) {
            WeaponSlotRender<? extends WeaponSlot> render = weaponRenders.get(i);
            aabb.union(render.getAabb());
        }
    }

    @Override
    public void renderAlpha() {
        if (!object.isSpawned()) return;

        for (int i = 0; i < moduleRenders.size(); i++) {
            moduleRenders.get(i).renderAlpha();
        }

        super.renderAlpha();
        renderGunSlots();

        Vector2f position = object.getPosition();
        Vector2f size = object.getSize();
        float yOffset = 3.2f + size.y / 4.0f;
        stringObject.renderWithShadow(BufferType.ENTITIES_ALPHA, lastPosition.x, lastPosition.y + yOffset, position.x,
                position.y + yOffset, 0.1f, 0.1f, 0.1f, 0.1f);
    }

    @Override
    public void renderAdditive() {
        if (object.isSpawned()) {
            renderGunSlotsAdditive();
            renderShield(object, object.getModules().getShield());
        } else {
            float delta = lastJumpDelta + (jumpDelta - lastJumpDelta) * renderer.getInterpolation();
            float size = 40.0f * delta;
            Vector4f effectsColor = object.getConfigData().getEffectsColor();

            spriteRenderer.addToRenderPipeLineSinCos(lastJumpPosition.x, lastJumpPosition.y, jumpPosition.x, jumpPosition.y,
                    object.getSin(), object.getCos(), size, size, effectsColor.x, effectsColor.y, effectsColor.z, delta,
                    JUMP_TEXTURE, BufferType.ENTITIES_ADDITIVE);
        }
    }

    @Override
    public void renderDebug() {
        if (object.isSpawned()) {
            super.renderDebug();
        }
    }

    private void renderGunSlots() {
        for (int i = 0, size = weaponRenders.size(); i < size; i++) {
            weaponRenders.get(i).renderAlpha(lastSin, lastCos, object.getSin(), object.getCos());
        }
    }

    private void renderGunSlotsAdditive() {
        for (int i = 0, size = weaponRenders.size(); i < size; i++) {
            weaponRenders.get(i).renderAdditive(lastSin, lastCos, object.getSin(), object.getCos());
        }
    }

    private void renderShield(Ship ship, Shield shield) {
        if (shield != null && shield.isShieldAlive()) {
            Vector2f diameter = shield.getDiameter();
            float size = shield.getSize().x;
            Vector4f color = ship.getConfigData().getEffectsColor();
            spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, ship.getPosition().x, ship.getPosition().y,
                    lastSin, lastCos, ship.getSin(), ship.getCos(), diameter.x * size, diameter.y * size, color.x, color.y,
                    color.z, color.w, shieldTexture, BufferType.ENTITIES_ADDITIVE);
        }
    }

    public void createName() {
        stringObject.setString(object.getName());
    }

    public void onWeaponShot(WeaponSlot weaponSlot) {
        for (int i = 0; i < weaponRenders.size(); i++) {
            WeaponSlotRender<? extends WeaponSlot> render = weaponRenders.get(i);
            if (render.getObject().getId() == weaponSlot.getId()) {
                render.onShot();
                break;
            }
        }
    }

    public void removeWeaponRender(int id) {
        weaponRenders.removeIf(weaponSlotRender -> weaponSlotRender.getObject().getId() == id);
    }

    @Nullable
    public WeaponSlotRender<? extends WeaponSlot> getWeaponRender(int id) {
        for (int i = 0; i < weaponRenders.size(); i++) {
            WeaponSlotRender<? extends WeaponSlot> render = weaponRenders.get(i);
            if (render.getObject().getId() == id) {
                return render;
            }
        }

        return null;
    }

    public AbstractTexture getWeaponSlotTexture(int id) {
        return weaponRenders.get(id).getTexture();
    }
}