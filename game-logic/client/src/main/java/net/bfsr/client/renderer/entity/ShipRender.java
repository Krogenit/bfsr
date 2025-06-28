package net.bfsr.client.renderer.entity;

import gnu.trove.map.TMap;
import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.font.FontType;
import net.bfsr.client.particle.effect.EngineEffects;
import net.bfsr.client.particle.effect.JumpEffects;
import net.bfsr.client.renderer.component.ModuleRenderer;
import net.bfsr.client.renderer.component.WeaponRenderRegistry;
import net.bfsr.client.renderer.component.WeaponSlotRender;
import net.bfsr.config.entity.ship.EngineData;
import net.bfsr.config.entity.ship.EnginesData;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.MaterialType;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.engine.world.entity.SpawnAccumulator;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.entity.ship.ShipJumpInEvent;
import net.bfsr.event.module.ModuleDestroyEvent;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class ShipRender extends DamageableRigidBodyRenderer {
    private static final AbstractTexture PARTICLE_JUMP_TEXTURE = Engine.getAssetsManager().getTexture(TextureRegister.particleJump);
    private static final AbstractTexture REACTOR_MODULE_TEXTURE = Engine.getAssetsManager().getTexture(TextureRegister.moduleReactor);
    private static final AbstractTexture ENGINE_MODULE_TEXTURE = Engine.getAssetsManager().getTexture(TextureRegister.moduleEngine);
    private static final AbstractTexture SHIELD_MODULE_TEXTURE = Engine.getAssetsManager().getTexture(TextureRegister.moduleShield);
    public static final float SHIELD_SIZE = 1.25f;

    private final Client client = Client.get();
    private final EngineEffects engineEffects = client.getParticleEffects().getEngineEffects();
    private final JumpEffects jumpEffects = client.getParticleEffects().getJumpEffects();

    protected final Ship ship;
    private final Label label = new Label(FontType.XOLONIUM.getFontName(), 24, StringOffsetType.CENTERED, BufferType.ENTITIES_ALPHA)
            .setShadow(true).setShadowOffsetX(4).setShadowOffsetY(-4);

    private final List<WeaponSlotRender> weaponRenders = new ArrayList<>();

    private final Vector2f jumpPosition = new Vector2f();
    private final Vector2f lastJumpPosition = new Vector2f();
    private float jumpDelta;
    private float jumpEffectSize;

    private final EnumMap<Direction, List<SpawnAccumulator>> engineAccumulators = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, Runnable> engineEffectsRunnable = new EnumMap<>(Direction.class);
    protected final List<ModuleRenderer> moduleRenders = new ArrayList<>();
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final Vector2f rotateToVector = new Vector2f();

    private float labelYOffset;
    private int spawnEffectId = -1;
    private int shieldId = -1;

    @Getter
    private final AbstractTexture shieldTexture;

    public ShipRender(Ship ship) {
        super(Engine.getAssetsManager().getTexture(ship.getConfigData().getTexture()), ship);
        this.ship = ship;
        this.jumpPosition.set(ship.getJumpPosition());
        this.lastJumpPosition.set(jumpPosition);

        createWeaponSlotsRenders(ship);
        initEngineEffectsRunnable(ship);
        createModuleRenders(ship);

        ship.getShipEventBus().register(this);

        ShipData configData = ship.getConfigData();
        shieldTexture = renderer.getTextureGenerator().generateShieldTexture(texture, getRenderer(), configData.getShieldOutlineOffset(),
                configData.getShieldBlurSize());
    }

    @Override
    public void init() {
        super.init();

        if (!ship.isSpawned()) {
            Vector4f effectsColor = ship.getConfigData().getEffectsColor();
            jumpEffectSize = 1.5f * Math.max(ship.getSizeX(), ship.getSizeY()) + 0.5f;
            spawnEffectId = spriteRenderer.add(jumpPosition.x, jumpPosition.y, ship.getSin(), ship.getCos(), 0.0f, 0.0f, effectsColor.x,
                    effectsColor.y, effectsColor.z, 0.0f, PARTICLE_JUMP_TEXTURE.getTextureHandle(), BufferType.ENTITIES_ADDITIVE);
        }

        Shield shield = ship.getModules().getShield();
        if (shield != null) {
            Vector4f color = ship.getConfigData().getEffectsColor();
            shieldId = spriteRenderer.add(ship.getX(), ship.getY(), ship.getSin(), ship.getCos(), ship.getSizeX() * SHIELD_SIZE,
                    ship.getSizeY() * SHIELD_SIZE, color.x, color.y, color.z, 0.25f, shieldTexture.getTextureHandle(),
                    maskTexture.getTextureHandle(), MaterialType.SHIELD, BufferType.ENTITIES_BACKGROUND_ADDITIVE);
        }
    }

    private void createWeaponSlotsRenders(Ship ship) {
        Modules modules = ship.getModules();
        List<WeaponSlot> weaponSlots = modules.getWeaponSlots();
        WeaponRenderRegistry weaponRenderRegistry = client.getEntityRenderer().getWeaponRenderRegistry();
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            WeaponSlotRender render = weaponRenderRegistry.createRender(weaponSlot);
            render.init();
            weaponRenders.add(render);
            weaponSlot.getWeaponSlotEventBus().addOneTimeListener(WeaponSlotRemovedEvent.class,
                    event -> {
                        render.clear();
                        weaponRenders.remove(render);
                    });
            weaponSlot.getWeaponSlotEventBus().addListener(WeaponShotEvent.class, event -> render.onShot());
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
        Vector4f effectsColor = ship.getConfigData().getEffectsColor();
        Vector2 shipVelocity = body.getLinearVelocity();

        Direction[] directions = Direction.VALUES;
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            Runnable runnable;
            if (direction == Direction.STOP) {
                runnable = () -> {
                    float shipX = ship.getX();
                    float shipY = ship.getY();
                    float velocityX = -(float) body.getLinearVelocity().x;
                    float velocityY = -(float) body.getLinearVelocity().y;

                    if (Math.abs(velocityX) > 0.5f) {
                        engineEffectsRunnable.get(rigidBodyUtils.calculateDirectionToPoint(ship, velocityX + shipX, shipY)).run();
                    }

                    if (Math.abs(velocityY) > 0.5f) {
                        engineEffectsRunnable.get(rigidBodyUtils.calculateDirectionToPoint(ship, shipX, velocityY + shipY)).run();
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
                            float shipX = ship.getX();
                            float shipY = ship.getY();
                            float sin = ship.getSin();
                            float cos = ship.getCos();

                            for (int j = 0; j < engineDataList.size(); j++) {
                                if (!engineModules.get(j).isDead()) {
                                    Vector2f effectPosition = engineDataList.get(j).effectPosition();
                                    RotationHelper.rotate(sin, cos, effectPosition.x, effectPosition.y, rotateToVector);
                                    engineEffects.smallEngine(shipX + rotateToVector.x, shipY + rotateToVector.y, sin,
                                            cos, 1.0f, shipVelocity.x / 50.0f, shipVelocity.y / 50.0f,
                                            effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, accumulators.get(j));
                                }
                            }
                        };
                    } else {
                        runnable = () -> {
                            float shipX = ship.getX();
                            float shipY = ship.getY();
                            float sin = ship.getSin();
                            float cos = ship.getCos();

                            for (int j = 0; j < engineDataList.size(); j++) {
                                if (!engineModules.get(j).isDead()) {
                                    Vector2f effectPosition = engineDataList.get(j).effectPosition();
                                    RotationHelper.rotate(sin, cos, effectPosition.x, effectPosition.y, rotateToVector);
                                    engineEffects.secondaryEngine(shipX + rotateToVector.x, shipY + rotateToVector.y,
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
        Modules modules = ship.getModules();

        moduleRenders.add(new ModuleRenderer(ship, modules.getReactor(), REACTOR_MODULE_TEXTURE));

        Shield shield = modules.getShield();
        if (!shield.isDead()) {
            addModuleRenderer(shield, new ModuleRenderer(ship, shield, SHIELD_MODULE_TEXTURE));
        }

        Engines enginesModule = modules.getEngines();
        TMap<Direction, EnginesData> engines = ship.getConfigData().getEngines();
        engines.forEachEntry((direction, enginesData) -> {
            List<EngineData> engineDataList = enginesData.engines();
            for (int i = 0; i < engineDataList.size(); i++) {
                net.bfsr.entity.ship.module.engine.Engine engine = enginesModule.getEngines(direction).get(i);
                if (!engine.isDead()) {
                    addModuleRenderer(engine, new ModuleRenderer(ship, engine, ENGINE_MODULE_TEXTURE, direction));
                }
            }

            return true;
        });

        for (int i = 0; i < moduleRenders.size(); i++) {
            moduleRenders.get(i).init();
        }
    }

    private void addModuleRenderer(DamageableModule module, ModuleRenderer moduleRenderer) {
        moduleRenders.add(moduleRenderer);
        module.getModuleEventBus().addOneTimeListener(ModuleDestroyEvent.class, event -> {
            moduleRenderer.clear();
            moduleRenders.remove(moduleRenderer);
        });
    }

    @Override
    public void update() {
        super.update();

        for (int i = 0; i < weaponRenders.size(); i++) {
            weaponRenders.get(i).update();
        }

        for (int i = 0; i < moduleRenders.size(); i++) {
            moduleRenders.get(i).update();
        }

        ship.getMoveDirections().forEach(direction -> {
            engineEffectsRunnable.get(direction).run();
            return true;
        });
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();

        if (!ship.isSpawned()) {
            Vector2f objectJumpPosition = ship.getJumpPosition();
            jumpDelta = 1.0f - ship.getJumpTimer() / (float) ship.getJumpTimeInFrames();
            jumpPosition.set(objectJumpPosition.x + (ship.getX() - objectJumpPosition.x) * jumpDelta * 0.9f,
                    objectJumpPosition.y + (ship.getY() - objectJumpPosition.y) * jumpDelta * 0.9f);
        } else {
            for (int i = 0; i < weaponRenders.size(); i++) {
                weaponRenders.get(i).postWorldUpdate();
            }

            for (int i = 0; i < moduleRenders.size(); i++) {
                moduleRenders.get(i).postWorldUpdate();
            }
        }
    }

    @Override
    protected void updateLastRenderValues() {
        super.updateLastRenderValues();
        if (!ship.isSpawned()) {
            lastJumpPosition.set(jumpPosition);
            spriteRenderer.setLastPosition(spawnEffectId, BufferType.ENTITIES_ADDITIVE, jumpPosition.x, jumpPosition.y);

            float size = jumpEffectSize * jumpDelta;
            spriteRenderer.setLastSize(spawnEffectId, BufferType.ENTITIES_ADDITIVE, size, size);

            spriteRenderer.setLastColorAlpha(spawnEffectId, BufferType.ENTITIES_ADDITIVE, jumpDelta);
        } else {
            label.updateLastPosition(object.getX(), object.getY());

            Shield shield = ship.getModules().getShield();
            if (shieldId != -1 && shield != null) {
                spriteRenderer.setLastPosition(shieldId, BufferType.ENTITIES_BACKGROUND_ADDITIVE, ship.getX(), ship.getY());
                spriteRenderer.setLastRotation(shieldId, BufferType.ENTITIES_BACKGROUND_ADDITIVE, ship.getSin(), ship.getCos());
                float finalShieldSize = shield.getSizeX() * SHIELD_SIZE;
                spriteRenderer.setLastSize(shieldId, BufferType.ENTITIES_BACKGROUND_ADDITIVE, ship.getSizeX() * finalShieldSize,
                        ship.getSizeY() * finalShieldSize);
            }
        }
    }

    @Override
    protected void updateRenderValues() {
        super.updateRenderValues();

        if (!ship.isSpawned()) {
            spriteRenderer.setPosition(spawnEffectId, BufferType.ENTITIES_ADDITIVE, jumpPosition.x, jumpPosition.y);
            float size = jumpEffectSize * jumpDelta;
            spriteRenderer.setSize(spawnEffectId, BufferType.ENTITIES_ADDITIVE, size, size);
            spriteRenderer.setColorAlpha(spawnEffectId, BufferType.ENTITIES_ADDITIVE, jumpDelta);
        } else {
            Shield shield = ship.getModules().getShield();
            if (shieldId != -1 && shield != null) {
                spriteRenderer.setPosition(shieldId, BufferType.ENTITIES_BACKGROUND_ADDITIVE, ship.getX(), ship.getY());
                spriteRenderer.setRotation(shieldId, BufferType.ENTITIES_BACKGROUND_ADDITIVE, ship.getSin(), ship.getCos());
                float finalShieldSize = shield.getSizeX() * SHIELD_SIZE;
                spriteRenderer.setSize(shieldId, BufferType.ENTITIES_BACKGROUND_ADDITIVE, ship.getSizeX() * finalShieldSize,
                        ship.getSizeY() * finalShieldSize);
            }

            label.updatePosition(object.getX(), object.getY());
        }
    }

    @Override
    protected void updateAABB() {
        super.updateAABB();

        float x = object.getX();
        float y = object.getY();
        float halfStringWidth = label.getWidth() / (label.getFontSize() * 14.0f);
        aabb.combine(x - halfStringWidth, y + labelYOffset - label.getHeight() * 0.005f, x + halfStringWidth, y);
    }

    @Override
    public void render() {
        if (!ship.isSpawned()) {
            spriteRenderer.addDrawCommand(spawnEffectId, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ADDITIVE);
            return;
        }

        renderModules();

        super.render();
        renderGunSlots();

        label.render(0, 0);

        renderShield();
    }

    protected void renderModules() {
        for (int i = 0; i < moduleRenders.size(); i++) {
            moduleRenders.get(i).render();
        }
    }

    protected void renderShield() {
        Shield shield = ship.getModules().getShield();
        if (shieldId != -1 && shield != null && shield.isAlive()) {
            spriteRenderer.addDrawCommand(shieldId, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX,
                    BufferType.ENTITIES_BACKGROUND_ADDITIVE);
        }
    }

    @Override
    public void renderDebug() {
        if (ship.isSpawned()) {
            super.renderDebug();
        }
    }

    protected void renderGunSlots() {
        for (int i = 0, size = weaponRenders.size(); i < size; i++) {
            weaponRenders.get(i).render();
        }
    }

    private void createName() {
        super.updateAABB();
        labelYOffset = getStringYPosition();
        float yOffset = labelYOffset * 200.0f - label.getHeight();
        label.setString(ship.getName(), 0, yOffset).scale(0.005f, 0.005f);
        label.getLabelRenderer().create(ship.getX(), ship.getY());
    }

    private float getStringYPosition() {
        return -(geometryAABB.getMaxY() - geometryAABB.getMinY()) * 0.5f - 0.05f;
    }

    @EventHandler
    public EventListener<ShipJumpInEvent> shipJumpInEvent() {
        return event -> {
            Ship ship = event.ship();
            Vector2 velocity = ship.getLinearVelocity();
            Vector4f effectsColor = ship.getConfigData().getEffectsColor();
            jumpEffects.jump(ship.getX(), ship.getY(), jumpEffectSize * 1.25f, velocity.x * 0.5f, velocity.y * 0.5f,
                    effectsColor.x, effectsColor.y, effectsColor.z, 1.0f);
            createName();
            if (spawnEffectId != -1) {
                spriteRenderer.removeObject(spawnEffectId, BufferType.ENTITIES_ADDITIVE);
                spawnEffectId = -1;
            }
        };
    }

    public AbstractTexture getWeaponSlotTexture(int id) {
        return weaponRenders.get(id).getTexture();
    }

    @Override
    public void clear() {
        super.clear();
        ship.getShipEventBus().unregister(this);

        for (int i = 0; i < weaponRenders.size(); i++) {
            weaponRenders.get(i).clear();
        }

        for (int i = 0; i < moduleRenders.size(); i++) {
            moduleRenders.get(i).clear();
        }

        label.getRenderer().remove();
        if (spawnEffectId != -1) {
            spriteRenderer.removeObject(spawnEffectId, BufferType.ENTITIES_ADDITIVE);
        }

        if (shieldId != -1) {
            spriteRenderer.removeObject(shieldId, BufferType.ENTITIES_BACKGROUND_ADDITIVE);
        }
    }
}