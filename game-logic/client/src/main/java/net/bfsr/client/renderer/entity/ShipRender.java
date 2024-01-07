package net.bfsr.client.renderer.entity;

import gnu.trove.map.TMap;
import lombok.Getter;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.EngineEffects;
import net.bfsr.client.renderer.component.WeaponSlotBeamRender;
import net.bfsr.client.renderer.component.WeaponSlotRender;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.config.entity.ship.EngineData;
import net.bfsr.config.entity.ship.EnginesData;
import net.bfsr.damage.DamageMask;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static net.bfsr.math.RigidBodyUtils.ROTATE_TO_VECTOR;

public class ShipRender extends RigidBodyRender<Ship> {
    private static final AbstractTexture JUMP_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.particleJump);
    private static final AbstractTexture REACTOR_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.moduleReactor);
    private static final AbstractTexture ENGINE_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.moduleEngine);
    private static final AbstractTexture SHIELD_TEXTURE = Engine.assetsManager.getTexture(TextureRegister.moduleShield);

    private final StringObject stringObject = new StringObject(FontType.XOLONIUM, 14, StringOffsetType.CENTERED);
    @Getter
    private final DamageMaskTexture maskTexture;
    private final List<WeaponSlotRender<? extends WeaponSlot>> weaponSlots = new ArrayList<>();
    private final AbstractTexture shieldTexture;

    private final Vector2f jumpPosition = new Vector2f();
    private final Vector2f lastJumpPosition = new Vector2f();
    private float lastJumpDelta;
    private float jumpDelta;

    private final EnumMap<Direction, List<SpawnAccumulator>> engineAccumulators = new EnumMap<>(Direction.class);

    public ShipRender(Ship ship) {
        super(Engine.assetsManager.getTexture(ship.getConfigData().getTexture()), ship);

        DamageMask mask = ship.getMask();
        maskTexture = new DamageMaskTexture(mask.getWidth(), mask.getHeight(),
                Engine.renderer.createByteBuffer(mask.getWidth() * mask.getHeight()));
        maskTexture.createWhiteMask();

        shieldTexture = Engine.assetsManager.getTexture(ship.getModules().getShield().getShieldData().getTexturePath());

        List<WeaponSlot> weaponSlots1 = object.getModules().getWeaponSlots();
        for (int i = 0; i < weaponSlots1.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots1.get(i);
            if (weaponSlot.getClass() == WeaponSlotBeam.class) {
                weaponSlots.add(new WeaponSlotBeamRender(((WeaponSlotBeam) weaponSlot)));
            } else {
                weaponSlots.add(new WeaponSlotRender<>(weaponSlot));
            }
        }

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
    }

    @Override
    public void update() {
        if (!object.isSpawned()) {
            lastJumpDelta = jumpDelta;
            lastJumpPosition.set(jumpPosition);
        }

        lastSin = object.getSin();
        lastCos = object.getCos();
        lastPosition.set(object.getPosition());

        for (int i = 0; i < weaponSlots.size(); i++) {
            weaponSlots.get(i).update();
        }

        object.getMoveDirections().forEach(this::spawnEngineParticles);

        maskTexture.updateEffects();
    }

    private void spawnEngineParticles(Direction direction) {
        Body body = object.getBody();
        Vector2f shipPos = object.getPosition();

        if (direction == Direction.STOP) {
            float x = -(float) body.getLinearVelocity().x;
            float y = -(float) body.getLinearVelocity().y;

            if (Math.abs(x) > 0.5f) {
                spawnEngineParticles(RigidBodyUtils.calculateDirectionToOtherObject(object, x + shipPos.x, shipPos.y));
            }

            if (Math.abs(y) > 0.5f) {
                spawnEngineParticles(RigidBodyUtils.calculateDirectionToOtherObject(object, shipPos.x, y + shipPos.y));
            }

            return;
        }

        float sin = object.getSin();
        float cos = object.getCos();
        Vector4f effectsColor = object.getConfigData().getEffectsColor();

        EnginesData enginesData = object.getConfigData().getEngines().get(direction);
        if (enginesData == null) return;

        List<EngineData> engines = enginesData.engines();
        Vector2 shipVelocity = body.getLinearVelocity();
        List<net.bfsr.entity.ship.module.engine.Engine> engineModules = object.getModules().getEngines().getEngines(direction);

        List<SpawnAccumulator> accumulators = engineAccumulators.get(direction);
        if (direction == Direction.FORWARD) {
            for (int i = 0; i < engines.size(); i++) {
                if (engineModules.get(i).isDead()) continue;
                Vector2f effectPosition = engines.get(i).effectPosition();
                RotationHelper.rotate(sin, cos, effectPosition.x, effectPosition.y, ROTATE_TO_VECTOR);
                EngineEffects.smallEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, sin, cos, 10.0f,
                        (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f,
                        effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, accumulators.get(i));
            }
        } else {
            for (int i = 0; i < engines.size(); i++) {
                if (engineModules.get(i).isDead()) continue;
                Vector2f effectPosition = engines.get(i).effectPosition();
                RotationHelper.rotate(sin, cos, effectPosition.x, effectPosition.y, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y,
                        accumulators.get(i));
            }
        }
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();

        for (int i = 0; i < weaponSlots.size(); i++) {
            weaponSlots.get(i).postWorldUpdate();
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

        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlotRender<? extends WeaponSlot> render = weaponSlots.get(i);
            aabb.union(render.getAabb());
        }
    }

    @Override
    public void renderAlpha() {
        if (object.isSpawned()) {
            float sin = object.getSin();
            float cos = object.getCos();
            Vector2f position = object.getPosition();

            renderModule(object.getConfigData().getReactorPolygon(), REACTOR_TEXTURE, position, sin, cos);

            Modules modules = object.getModules();
            if (!modules.getShield().isDead()) {
                renderModule(object.getConfigData().getShieldPolygon(), SHIELD_TEXTURE, position, sin, cos);
            }

            Engines enginesModule = modules.getEngines();
            TMap<Direction, EnginesData> engines = object.getConfigData().getEngines();
            engines.forEachEntry((direction, enginesData) -> {
                List<EngineData> engineDataList = enginesData.engines();
                for (int i = 0; i < engineDataList.size(); i++) {
                    EngineData engineData = engineDataList.get(i);
                    if (!enginesModule.getEngines(direction).get(i).isDead()) {
                        renderModule(engineData.polygons().get(0), ENGINE_TEXTURE, position, sin, cos, direction);
                    }
                }

                return true;
            });

            Vector2f scale = object.getSize();
            spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y,
                    lastSin, lastCos, sin, cos, scale.x, scale.y, 1.0f, 1.0f, 1.0f, 1.0f, texture, maskTexture,
                    BufferType.ENTITIES_ALPHA);

            renderGunSlots();
            float yOffset = 3.2f + scale.y / 4.0f;
            stringObject.renderWithShadow(BufferType.ENTITIES_ALPHA, lastPosition.x, lastPosition.y + yOffset, position.x,
                    position.y + yOffset, 0.1f, 0.1f, 0.1f, 0.1f);
        }
    }

    private void renderModule(Polygon polygon, AbstractTexture texture, Vector2f position, float sin, float cos,
                              Direction direction) {
        Vector2 center = polygon.getCenter();
        float centerX = (float) center.x;
        float centerY = (float) center.y;
        org.dyn4j.geometry.AABB aabb1 = new AABB(0, 0, 0, 0);
        polygon.computeAABB(aabb1);
        float sizeX = (float) (aabb1.getMaxX() - aabb1.getMinX());
        float sizeY = (float) (aabb1.getMaxY() - aabb1.getMinY());

        float lastX = lastCos * centerX - lastSin * centerY + lastPosition.x;
        float lastY = lastSin * centerX + lastCos * centerY + lastPosition.y;
        float x = cos * centerX - sin * centerY + position.x;
        float y = sin * centerX + cos * centerY + position.y;

        float sin1;
        float cos1;
        if (direction == Direction.FORWARD) {
            sin1 = LUT.sin(-MathUtils.HALF_PI);
            cos1 = LUT.cos(-MathUtils.HALF_PI);
        } else if (direction == Direction.BACKWARD) {
            sin1 = LUT.sin(MathUtils.HALF_PI);
            cos1 = LUT.cos(MathUtils.HALF_PI);
        } else if (direction == Direction.LEFT) {
            sin1 = LUT.sin(MathUtils.PI);
            cos1 = LUT.cos(MathUtils.PI);
        } else {
            sin1 = 0;
            cos1 = 1;
        }

        if (sizeY > sizeX) {
            float temp = sizeX;
            sizeX = sizeY;
            sizeY = temp;
        }

        float cost = cos1 * cos - sin1 * sin;
        float sint = sin1 * cos + cos1 * sin;
        float lastcost = cos1 * lastCos - sin1 * lastSin;
        float lastsint = sin1 * lastCos + cos1 * lastSin;

        spriteRenderer.addToRenderPipeLineSinCos(lastX, lastY, x, y, lastsint, lastcost, sint, cost, sizeX, sizeY, 1.0f, 1.0f,
                1.0f, 1.0f, texture, BufferType.ENTITIES_ALPHA);
    }

    private void renderModule(Polygon polygon, AbstractTexture texture, Vector2f position, float sin, float cos) {
        Vector2 center = polygon.getCenter();
        float centerX = (float) center.x;
        float centerY = (float) center.y;
        org.dyn4j.geometry.AABB aabb1 = new AABB(0, 0, 0, 0);
        polygon.computeAABB(aabb1);
        float sizeX = (float) (aabb1.getMaxX() - aabb1.getMinX());
        float sizeY = (float) (aabb1.getMaxY() - aabb1.getMinY());

        float lastX = lastCos * centerX - lastSin * centerY + lastPosition.x;
        float lastY = lastSin * centerX + lastCos * centerY + lastPosition.y;
        float x = cos * centerX - sin * centerY + position.x;
        float y = sin * centerX + cos * centerY + position.y;

        if (sizeY > sizeX) {
            spriteRenderer.addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, sizeX, sizeY, 1.0f, 1.0f,
                    1.0f, 1.0f, texture, BufferType.ENTITIES_ALPHA);
        } else {
            float sin1 = LUT.sin(MathUtils.HALF_PI);
            float cos1 = LUT.cos(MathUtils.HALF_PI);

            float cost = cos1 * cos - sin1 * sin;
            float sint = sin1 * cos + cos1 * sin;
            float lastcost = cos1 * lastCos - sin1 * lastSin;
            float lastsint = sin1 * lastCos + cos1 * lastSin;

            spriteRenderer.addToRenderPipeLineSinCos(lastX, lastY, x, y, lastsint, lastcost, sint, cost, sizeY, sizeX, 1.0f, 1.0f,
                    1.0f, 1.0f, texture, BufferType.ENTITIES_ALPHA);
        }
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
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            weaponSlots.get(i).renderAlpha(lastSin, lastCos, object.getSin(), object.getCos());
        }
    }

    private void renderGunSlotsAdditive() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            weaponSlots.get(i).renderAdditive(lastSin, lastCos, object.getSin(), object.getCos());
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

    @Override
    public void updateDamageMask(int x, int y, int width, int height, ByteBuffer byteBuffer) {
        maskTexture.upload(x, y, width, height, byteBuffer);
    }

    public void onWeaponShot(WeaponSlot weaponSlot) {
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlotRender<? extends WeaponSlot> render = weaponSlots.get(i);
            if (render.getObject().getId() == weaponSlot.getId()) {
                render.onShot();
                break;
            }
        }
    }

    public void removeWeaponRender(int id) {
        weaponSlots.removeIf(weaponSlotRender -> weaponSlotRender.getObject().getId() == id);
    }

    @Nullable
    public WeaponSlotRender<? extends WeaponSlot> getWeaponRender(int id) {
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlotRender<? extends WeaponSlot> render = weaponSlots.get(i);
            if (render.getObject().getId() == id) {
                return render;
            }
        }

        return null;
    }

    public AbstractTexture getWeaponSlotTexture(int id) {
        return weaponSlots.get(id).getTexture();
    }

    @Override
    public void clear() {
        maskTexture.delete();
    }
}