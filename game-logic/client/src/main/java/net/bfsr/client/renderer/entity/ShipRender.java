package net.bfsr.client.renderer.entity;

import lombok.Getter;
import net.bfsr.client.gui.font.StringObject;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.EngineEffects;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.component.WeaponSlotBeamRender;
import net.bfsr.client.renderer.component.WeaponSlotRender;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.Shield;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static net.bfsr.math.RigidBodyUtils.ROTATE_TO_VECTOR;

public class ShipRender extends Render<Ship> {
    private static final AbstractTexture JUMP_TEXTURE = Engine.assetsManager.textureLoader.getTexture(TextureRegister.particleJump);

    private final StringObject stringObject = new StringObject(FontType.XOLONIUM, 14, StringOffsetType.CENTERED);
    @Getter
    private final DamageMaskTexture maskTexture;
    private final AbstractTexture textureDamage;
    private final List<WeaponSlotRender<?>> weaponSlots = new ArrayList<>();
    private final AbstractTexture shieldTexture;

    private final Vector2f jumpPosition = new Vector2f();
    private final Vector2f lastJumpPosition = new Vector2f();
    private float lastJumpDelta;
    private float jumpDelta;

    private final SpawnAccumulator engineSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator leftEngineSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator rightEngineSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator frontEngineSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator leftBackEngineSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator rightBackEngineSpawnAccumulator = new SpawnAccumulator();

    public ShipRender(Ship ship) {
        super(Engine.assetsManager.textureLoader.getTexture(ship.getShipData().getTexture()), ship);
        textureDamage = Engine.assetsManager.textureLoader.getTexture(ship.getShipData().getDamageTexture());

        maskTexture = new DamageMaskTexture(texture.getWidth(), texture.getHeight(), Engine.renderer.createByteBuffer(texture.getWidth() * texture.getHeight()));
        maskTexture.createWhiteMask();

        this.shieldTexture = Engine.assetsManager.textureLoader.getTexture(ship.getShield().getShieldData().getTexturePath());

        List<WeaponSlot> weaponSlots1 = object.getWeaponSlots();
        for (int i = 0; i < weaponSlots1.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots1.get(i);
            if (weaponSlot.getClass() == WeaponSlotBeam.class) {
                weaponSlots.add(new WeaponSlotBeamRender(((WeaponSlotBeam) weaponSlot)));
            } else {
                weaponSlots.add(new WeaponSlotRender<>(weaponSlot));
            }
        }
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

            if (Math.abs(x) > 10) {
                spawnEngineParticles(RigidBodyUtils.calculateDirectionToOtherObject(object, x + shipPos.x, shipPos.y));
            }

            if (Math.abs(y) > 10) {
                spawnEngineParticles(RigidBodyUtils.calculateDirectionToOtherObject(object, shipPos.x, y + shipPos.y));
            }

            return;
        }

        float sin = object.getSin();
        float cos = object.getCos();
        Vector4f effectsColor = object.getShipData().getEffectsColor();

        if (object.getFaction() == Faction.HUMAN) {
            if (direction == Direction.FORWARD) {
                RotationHelper.rotate(sin, cos, -2.3f, 0, ROTATE_TO_VECTOR);
                Vector2 shipVelocity = body.getLinearVelocity();
                EngineEffects.smallEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, sin, cos, 10.0f,
                        (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f,
                        effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, engineSpawnAccumulator);
            } else if (direction == Direction.LEFT) {
                RotationHelper.rotate(sin, cos, -0.5f, 3.0f, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, leftEngineSpawnAccumulator);
            } else if (direction == Direction.RIGHT) {
                RotationHelper.rotate(sin, cos, -0.5f, -3.0f, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, rightEngineSpawnAccumulator);
            } else if (direction == Direction.BACKWARD) {
                RotationHelper.rotate(sin, cos, 3.0f, 0, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, frontEngineSpawnAccumulator);
            }
        } else if (object.getFaction() == Faction.SAIMON) {
            if (direction == Direction.FORWARD) {
                RotationHelper.rotate(sin, cos, -3.3f, 0, ROTATE_TO_VECTOR);
                Vector2 shipVelocity = body.getLinearVelocity();
                EngineEffects.smallEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, sin, cos, 10.0f,
                        (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f,
                        effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, engineSpawnAccumulator);
            } else if (direction == Direction.LEFT) {
                RotationHelper.rotate(sin, cos, -0, 3.0f, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, leftEngineSpawnAccumulator);
            } else if (direction == Direction.RIGHT) {
                RotationHelper.rotate(sin, cos, -0, -3.0f, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, rightEngineSpawnAccumulator);
            } else if (direction == Direction.BACKWARD) {
                RotationHelper.rotate(sin, cos, 5.0f, 0, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, frontEngineSpawnAccumulator);
            }
        } else {
            if (direction == Direction.FORWARD) {
                RotationHelper.rotate(sin, cos, -3.7f, 0, ROTATE_TO_VECTOR);
                Vector2 shipVelocity = body.getLinearVelocity();
                float velocityX = (float) shipVelocity.x / 50.0f;
                float velocityY = (float) shipVelocity.y / 50.0f;
                EngineEffects.smallEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, sin, cos, 10.0f, velocityX, velocityY,
                        effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, engineSpawnAccumulator);

                RotationHelper.rotate(sin, cos, -3.0f, 1.1f, ROTATE_TO_VECTOR);
                EngineEffects.smallEngineNoSmoke(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, sin, cos, 10.0f, velocityX, velocityY,
                        effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, leftBackEngineSpawnAccumulator);
                RotationHelper.rotate(sin, cos, -3.0f, -1.1f, ROTATE_TO_VECTOR);
                EngineEffects.smallEngineNoSmoke(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, sin, cos, 10.0f, velocityX, velocityY,
                        effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, rightBackEngineSpawnAccumulator);
            } else if (direction == Direction.LEFT) {
                RotationHelper.rotate(sin, cos, 0, 2.1f, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, leftEngineSpawnAccumulator);
            } else if (direction == Direction.RIGHT) {
                RotationHelper.rotate(sin, cos, 0, -2.1f, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, rightEngineSpawnAccumulator);
            } else if (direction == Direction.BACKWARD) {
                RotationHelper.rotate(sin, cos, 3.7f, 0, ROTATE_TO_VECTOR);
                EngineEffects.secondaryEngine(shipPos.x + ROTATE_TO_VECTOR.x, shipPos.y + ROTATE_TO_VECTOR.y, frontEngineSpawnAccumulator);
            }
        }
    }

    @Override
    public void postWorldUpdate() {
        updateAABB(object.getSin(), object.getCos());

        for (int i = 0; i < weaponSlots.size(); i++) {
            weaponSlots.get(i).postWorldUpdate();
        }

        if (!object.isSpawned()) {
            Vector2f position = object.getPosition();
            Vector2f objectJumpPosition = object.getJumpPosition();
            jumpDelta = 1.0f - object.getJumpTimer() / (float) object.getJumpTime();
            jumpPosition.set(objectJumpPosition.x + (position.x - objectJumpPosition.x) * jumpDelta * 0.9f,
                    objectJumpPosition.y + (position.y - objectJumpPosition.y) * jumpDelta * 0.9f);
        }
    }

    @Override
    public void renderAlpha() {
        if (object.isSpawned()) {
            float sin = object.getSin();
            float cos = object.getCos();
            Vector2f position = object.getPosition();
            Vector2f scale = object.getSize();
            Engine.renderer.spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos,
                    scale.x, scale.y, 1.0f, 1.0f, 1.0f, 1.0f, texture, maskTexture, BufferType.ENTITIES_ALPHA);

            Hull hull = object.getHull();
            if (hull.getHull() < hull.getMaxHull()) {
                float hp = hull.getHull() / hull.getMaxHull();
                Engine.renderer.spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos,
                        scale.x, scale.y, 1.0f, 1.0f, 1.0f, 1.0f - hp, textureDamage, maskTexture, BufferType.ENTITIES_ALPHA);
            }

            renderGunSlots();
            float yOffset = 3.2f + scale.y / 4.0f;
            stringObject.renderWithShadow(BufferType.ENTITIES_ALPHA, lastPosition.x, lastPosition.y + yOffset, position.x, position.y + yOffset,
                    0.1f, 0.1f, 0.1f, 0.1f);
        }
    }

    @Override
    public void renderAdditive() {
        if (object.isSpawned()) {
            renderGunSlotsAdditive();
            renderShield(object, object.getShield());
        } else {
            float delta = lastJumpDelta + (jumpDelta - lastJumpDelta) * Engine.renderer.getInterpolation();
            float size = 40.0f * delta;
            Vector4f effectsColor = object.getShipData().getEffectsColor();

            Engine.renderer.spriteRenderer.addToRenderPipeLineSinCos(lastJumpPosition.x, lastJumpPosition.y, jumpPosition.x, jumpPosition.y,
                    object.getSin(), object.getCos(), size, size, effectsColor.x, effectsColor.y, effectsColor.z, delta, JUMP_TEXTURE,
                    BufferType.ENTITIES_ADDITIVE);
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
            float size = shield.getSize();
            Vector4f color = ship.getShipData().getEffectsColor();
            Engine.renderer.spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, ship.getPosition().x, ship.getPosition().y,
                    lastSin, lastCos, ship.getSin(), ship.getCos(), diameter.x * size, diameter.y * size, color.x, color.y, color.z, color.w,
                    shieldTexture, BufferType.ENTITIES_ADDITIVE);
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
        weaponSlots.get(weaponSlot.getId()).onShot();
    }

    public WeaponSlotRender<?> getWeaponRender(int id) {
        return weaponSlots.get(id);
    }

    public AbstractTexture getWeaponSlotTexture(int id) {
        return weaponSlots.get(id).getTexture();
    }

    @Override
    public void clear() {
        maskTexture.delete();
    }
}