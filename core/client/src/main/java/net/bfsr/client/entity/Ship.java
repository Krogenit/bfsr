package net.bfsr.client.entity;

import lombok.Getter;
import net.bfsr.client.component.Damage;
import net.bfsr.client.component.Shield;
import net.bfsr.client.component.WeaponSlot;
import net.bfsr.client.component.WeaponSlotBeam;
import net.bfsr.client.core.Core;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.network.packet.common.PacketObjectPosition;
import net.bfsr.client.network.packet.common.PacketShipEngine;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.settings.Option;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Ship extends ShipCommon {
    private static Texture jumpTexture;

    @Getter
    private final Texture texture;

    private StringObject stringObject;

    protected Texture textureDamage;
    private List<Damage> damages;

    private Direction remoteMoveDirectionForEngineParticles;

    protected final Vector2f lastJumpPosition = new Vector2f();

    protected Ship(WorldClient world, int id, TextureRegister texture, TextureRegister textureDamage, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b) {
        super(world, id, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, false);
        this.texture = TextureLoader.getTexture(texture);
        this.textureDamage = TextureLoader.getTexture(textureDamage);
        this.damages = new ArrayList<>();
        RotationHelper.angleToVelocity(this.rotation + MathUtils.PI, -jumpSpeed * 6.0f, jumpVelocity);
        this.jumpPosition.set(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + y);
        this.effectsColor.set(r, g, b);
        setRotation(this.rotation);
        world.addShip(this);
        jumpTexture = TextureLoader.getTexture(TextureRegister.particleJump);
    }

    public void control() {
        if (destroyingTimer == 0) {
            if (body.isAtRest()) body.setAtRest(false);

            rotateToVector(Mouse.getWorldPosition(Core.get().getRenderer().getCamera()), engine.getRotationSpeed());

            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_W)) {
                move(this, Direction.FORWARD);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_S)) {
                move(this, Direction.BACKWARD);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_A)) {
                move(this, Direction.LEFT);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_D)) {
                move(this, Direction.RIGHT);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_X)) {
                move(this, Direction.STOP);
            }

            if (Mouse.isLeftDown()) {
                shoot();
            }

            if (Option.IS_DEBUG.getBoolean() && Keyboard.isKeyDown(GLFW.GLFW_KEY_R)) {
                float baseSize = 4.0f + scale.x * 0.25f;
                Random rand = world.getRand();
                Vector2f position = getPosition();
                Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion1, position.x, position.y));
                ParticleSpawner.spawnShockwave(0, position, baseSize + 3.0f);
                for (int i = 0; i < 8; i++) {
                    Vector2f velocity = getVelocity();
                    RotationHelper.angleToVelocity(rand.nextFloat() * MathUtils.TWO_PI, scale.x, angleToVelocity);
                    ParticleSpawner.spawnMediumGarbage(1, position.x + -scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f)), position.y + -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f)),
                            velocity.x + angleToVelocity.x, velocity.y + angleToVelocity.y, baseSize);
                }
                float size = (scale.x + scale.y) * 1.1f;
                ParticleSpawner.spawnSpark(position.x, position.y, size);
                ParticleSpawner.spawnLight(position.x, position.y, size, 4.0f * 6.0f, 1, 0.5f, 0.4f, 1.0f, 0.05f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
                ParticleSpawner.spawnRocketShoot(position.x, position.y, size);
            }
        }
    }

    @Override
    protected void updateJump() {
        lastJumpPosition.set(jumpPosition);
        super.updateJump();
    }

    @Override
    protected void updateShip() {
        super.updateShip();

        if (this == world.getPlayerShip()) {
            Core.get().sendPacket(new PacketObjectPosition(this));
            aliveTimer = 0;
        } else {
            if (remoteMoveDirectionForEngineParticles != null) {
                spawnEngineParticles(remoteMoveDirectionForEngineParticles);
            }
        }

        if (destroyingTimer > 0) {
            sparksTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (sparksTimer <= 0) {
                createSpark();
                sparksTimer = 25;
            }
        }
    }

    @Override
    protected void updateComponents() {
        super.updateComponents();
        for (int i = 0, size = damages.size(); i < size; i++) {
            Damage damage = damages.get(i);
            damage.update();
        }
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);
        for (int i = 0, size = damages.size(); i < size; i++) {
            Damage damage = damages.get(i);
            damage.updatePos();
        }
    }

    protected void createName() {
        if (name != null) {
            stringObject = new StringObject(FontType.XOLONIUM, name, 14, StringOffsetType.CENTERED);
            stringObject.compile();
        }
    }

    @Override
    protected void createSpark() {
        Random rand = world.getRand();
        Vector2f position = getPosition();
        Vector2f velocity = getVelocity();
        float randomVectorX = position.x + -scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f));
        float randomVectorY = position.y + -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f));
        float baseSize = 4.0f + scale.x * 0.25f;
        ParticleSpawner.spawnMediumGarbage(rand.nextInt(2) + 1, randomVectorX, randomVectorY, velocity.x * 0.02f, velocity.y * 0.02f, baseSize - rand.nextFloat() * 2.5f);
        ParticleSpawner.spawnSmallGarbage(4, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)), position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)),
                velocity.x * 0.001f, velocity.y * 0.001f, baseSize);
        ParticleSpawner.spawnShipOst(1 + rand.nextInt(3), randomVectorX, randomVectorY, velocity.x * 0.02f, velocity.y * 0.02f, 1.0f);
        ParticleSpawner.spawnLight(randomVectorX, randomVectorY, baseSize + rand.nextFloat() * 2.0f, 60.0f, 1.0f, 0.5f, 0.5f, 0.7f, 0.03f * 60.0f, false, RenderLayer.DEFAULT_ADDITIVE);
        ParticleSpawner.spawnSpark(randomVectorX, randomVectorY, baseSize + rand.nextFloat() * 2.0f);
        ParticleSpawner.spawnExplosion(randomVectorX, randomVectorY, baseSize + rand.nextFloat() * 2.0f);
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, randomVectorX, randomVectorY));
    }

    @Override
    protected void onShipSpawned() {
        Vector2f velocity = getVelocity();
        ParticleSpawner.spawnLight(position.x, position.y, velocity.x * 0.5f, velocity.y * 0.5f, 32.0f + scale.x * 0.25f, effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, 3.6f,
                true, RenderLayer.DEFAULT_ADDITIVE);
        ParticleSpawner.spawnDisableShield(position.x, position.y, velocity.x * 0.5f, velocity.y * 0.5f, 32.0f + scale.x * 0.25f, effectsColor.x, effectsColor.y, effectsColor.z, 1.0f);
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.jump, position.x, position.y));
    }

    @Override
    protected void onMove(Direction direction) {
        spawnEngineParticles(direction);
        if (this == world.getPlayerShip()) Core.get().sendPacket(new PacketShipEngine(id, direction.ordinal()));
    }

    @Override
    protected void onStopMove(Direction direction) {
        spawnEngineParticles(direction);
        if (this == world.getPlayerShip()) {
            Core.get().sendPacket(new PacketShipEngine(id, direction.ordinal()));
        }
    }

    @Override
    protected void onCollidedWithWreck(Contact contact, Vector2 normal) {
        if (shield instanceof Shield shield) {
            Vector4f color = shield.getColor();
            ParticleSpawner.spawnDirectedSpark(contact, normal, 4.5f, color.x, color.y, color.z, color.w);
        } else {
            ParticleSpawner.spawnDirectedSpark(contact, normal, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    protected void onShieldDamageByCollision(Contact contact, Vector2 normal) {
        Vector4f color = ((Shield) shield).getColor();
        ParticleSpawner.spawnDirectedSpark(contact, normal, 4.5f, color.x, color.y, color.z, color.w);
    }

    @Override
    protected void onHullDamageByCollision(Contact contact, Vector2 normal) {
        Random rand = world.getRand();
        ParticleSpawner.spawnDirectedSpark(contact, normal, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
        Vector2f angletovel = RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.15f);
        Vector2 point = contact.getPoint();
        ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), (float) point.x, (float) point.y, velocity.x * 0.25f + angletovel.x, velocity.y * 0.25f + angletovel.y, 2.0f * rand.nextFloat());
    }

    public void renderAdditive() {
        if (spawned) {
            int size = damages.size();
            for (int i = 0; i < size; i++) {
                Damage damage = damages.get(i);
                damage.renderEffects();
            }

            renderGunSlotsAdditive();
            renderShield();
        } else {
            float size = 40.0f * color.w;
            SpriteRenderer.INSTANCE.addToRenderPipeLine(lastJumpPosition.x, lastJumpPosition.y, jumpPosition.x, jumpPosition.y, rotation, size, size,
                    effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, jumpTexture, BufferType.ENTITIES_ADDITIVE);
        }
    }

    public void render() {
        if (spawned) {
            Vector2f position = getPosition();

            SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                    color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);

            if (hull.getHull() < hull.getMaxHull()) {
                float hp = hull.getHull() / hull.getMaxHull();
                SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                        1.0f, 1.0f, 1.0f, 1.0f - hp, textureDamage, BufferType.ENTITIES_ALPHA);
            }

            int size = damages.size();
            for (int i = 0; i < size; i++) {
                Damage damage = damages.get(i);
                damage.render();
            }

            renderGunSlots();
            float yOffset = 3.2f + scale.y / 4.0f;
            stringObject.renderWithShadow(BufferType.ENTITIES_ALPHA, lastPosition.x, lastPosition.y + yOffset, position.x, position.y + yOffset, 0.1f, 0.1f, 0.1f, 0.1f);
        }
    }

    private void renderGunSlots() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlotCommon weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.render();
        }
    }

    private void renderGunSlotsAdditive() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlotCommon weaponSlot = weaponSlots.get(i);
            if (weaponSlot instanceof WeaponSlot weaponSlot1) weaponSlot1.renderAdditive();
            else if (weaponSlot instanceof WeaponSlotBeam weaponSlot1) weaponSlot1.renderAdditive();
        }
    }

    private void renderShield() {
        if (shield != null) {
            ((Shield) shield).render();
        }
    }

    public void addDamage(Damage damage) {
        damages.add(damage);
    }

    public void setMoveDirection(Direction dir) {
        remoteMoveDirectionForEngineParticles = dir;
    }

    @Override
    public void setSpawned() {
        super.setSpawned();
        createName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        if (spawned) createName();
    }
}
