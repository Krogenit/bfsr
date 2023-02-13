package net.bfsr.entity.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.ai.Ai;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.ai.task.AiAttackTarget;
import net.bfsr.ai.task.AiSearchTarget;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.particle.Wreck;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.InstancedRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.component.Armor;
import net.bfsr.component.Engine;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.damage.Damage;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.Shield;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.*;
import net.bfsr.server.MainServer;
import net.bfsr.settings.EnumOption;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.TOITransformSavable;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Ship extends CollisionObject implements TOITransformSavable {
    private static Texture jumpTexture;

    @Getter
    private Armor armor;
    @Getter
    private Shield shield;
    @Getter
    private Engine engine;
    @Getter
    private Faction faction;
    @Getter
    private Reactor reactor;
    @Getter
    private Crew crew;
    @Getter
    private Hull hull;
    @Getter
    @Setter
    private Cargo cargo;

    @Getter
    private String name;
    private StringObject stringObject;

    private final List<Vector2f> weaponPositions = new ArrayList<>();
    @Getter
    private List<WeaponSlot> weaponSlots;
    private boolean spawned;
    private final Vector2f jumpVelocity = new Vector2f();
    private final Vector2f jumpPosition = new Vector2f();
    private final Vector2f lastJumpPosition = new Vector2f();
    private final float jumpSpeed = 25.0f;
    private final Vector3f effectsColor = new Vector3f();
    private int collisionTimer;
    private int destroingTimer, sparksTimer;
    protected int maxDestroingTimer, maxSparksTimer;

    @Getter
    @Setter
    private PlayerServer owner;
    private boolean controlledByPlayer;

    @Getter
    private final Ai ai;
    @Getter
    private CollisionObject lastAttacker, target;

    protected Texture textureDamage;
    private List<Damage> damages;

    private Direction remoteMoveDirectionForEngineParticles;
    /**
     * Saved transform before TOI solver
     */
    private final Transform savedTransform = new Transform();
    private boolean transformSaved;

    protected Ship(WorldServer world, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, boolean spawned) {
        super(world, world.getNextId(), x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 0.0f);
        RotationHelper.angleToVelocity(this.rotation + MathUtils.PI, -jumpSpeed * 6.0f, jumpVelocity);
        this.jumpPosition.set(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + y);
        this.effectsColor.set(r, g, b);
        setRotation(this.rotation);
        if (spawned) setSpawned();
        this.ai = new Ai(this);
        this.ai.setAggressiveType(AiAggressiveType.ATTACK);
        this.ai.addTask(new AiSearchTarget(this, 4000.0f));
        this.ai.addTask(new AiAttackTarget(this, 4000.0f));
        this.world.addShip(this);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    protected Ship(WorldClient world, int id, TextureRegister texture, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b) {
        super(world, id, texture, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 0.0f);
        this.damages = new ArrayList<>();
        RotationHelper.angleToVelocity(this.rotation + MathUtils.PI, -jumpSpeed * 6.0f, jumpVelocity);
        this.jumpPosition.set(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + y);
        this.effectsColor.set(r, g, b);
        setRotation(this.rotation);
        this.ai = new Ai(this);
        this.world.addShip(this);
        jumpTexture = TextureLoader.getTexture(TextureRegister.particleJump);
    }

    public abstract void init();

    public void control() {
        if (destroingTimer == 0) {
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

            if (EnumOption.IS_DEBUG.getBoolean() && Keyboard.isKeyDown(GLFW.GLFW_KEY_R)) {
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

    private void updateShip() {
        Vector2f position = getPosition();
        lastRotation = getRotation();
        lastPosition.set(position.x, position.y);

        if (world.isRemote()) {
            if (this == world.getPlayerShip()) {
                Core.get().sendPacket(new PacketObjectPosition(this));
                aliveTimer = 0;
            } else {
                if (remoteMoveDirectionForEngineParticles != null) {
                    spawnEngineParticles(remoteMoveDirectionForEngineParticles);
                }
            }
        }

        if (collisionTimer > 0) collisionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (world.isRemote()) {
            if (destroingTimer > 0) {
                sparksTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                if (sparksTimer <= 0) {
                    createSpark();
                    sparksTimer = 25;
                }
            }
        } else {
            WorldServer world = (WorldServer) this.world;
            PlayerServer player = world.getPlayer(name);
            if (controlledByPlayer) {
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearbyExcept(new PacketObjectPosition(this), position, WorldServer.PACKET_SPAWN_DISTANCE, player);
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
            } else {
                if (destroingTimer == 0 && ai != null) ai.update();

                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketObjectPosition(this), position, WorldServer.PACKET_SPAWN_DISTANCE);
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
            }

            if (destroingTimer > 0) {
                sparksTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                if (sparksTimer <= 0) {
                    createSpark();
                    sparksTimer = 25;
                }

                destroingTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                if (destroingTimer <= 0) {
                    destroyShip();
                }
            }
        }
    }

    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship otherShip) {
                Vector2f velocityDif = new Vector2f((float) (body.getLinearVelocity().x - this.body.getLinearVelocity().x),
                        (float) (body.getLinearVelocity().y - this.body.getLinearVelocity().y));
                float impactPowerForOther = (float) ((velocityDif.length()) *
                        (this.body.getMass().getMass() / body.getMass().getMass()));

                impactPowerForOther /= 400.0f;

                if (impactPowerForOther > 0.25f) otherShip.damageByCollision(this, impactPowerForOther, contact, normal);
            } else if (userData instanceof Wreck) {
                if (collisionTimer <= 0) {
                    collisionTimer = 2;
                    if (world.isRemote()) {
                        if (shield != null) {
                            Vector4f color = shield.getColor();
                            ParticleSpawner.spawnDirectedSpark(contact, normal, 4.5f, color.x, color.y, color.z, color.w);
                        } else {
                            ParticleSpawner.spawnDirectedSpark(contact, normal, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
                        }
                    }
                }
            }
        }
    }

    public void update() {
        super.update();

        if (spawned) {
            updateShip();
        } else {
            if (((jumpVelocity.x < 0 && jumpPosition.x <= position.x) || (jumpVelocity.x >= 0 && jumpPosition.x >= position.x)) && ((jumpVelocity.y < 0 && jumpPosition.y <= position.y)
                    || (jumpVelocity.y >= 0 && jumpPosition.y >= position.y))) {
                setSpawned();
                setVelocity(jumpVelocity.mul(0.26666668f));
                if (world.isRemote()) {
                    Vector2f velocity = getVelocity();
                    ParticleSpawner.spawnLight(position.x, position.y, velocity.x * 0.5f, velocity.y * 0.5f, 32.0f + scale.x * 0.25f, effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, 3.6f,
                            true, RenderLayer.DEFAULT_ADDITIVE);
                    ParticleSpawner.spawnDisableShield(position.x, position.y, velocity.x * 0.5f, velocity.y * 0.5f, 32.0f + scale.x * 0.25f, effectsColor.x, effectsColor.y, effectsColor.z, 1.0f);
                    Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.jump, position.x, position.y));
                }
            } else {
                lastJumpPosition.set(jumpPosition);
                jumpPosition.add(jumpVelocity.x * TimeUtils.UPDATE_DELTA_TIME, jumpVelocity.y * TimeUtils.UPDATE_DELTA_TIME);
                color.w += 1.5f * TimeUtils.UPDATE_DELTA_TIME;
            }
        }
    }

    @Override
    public void postPhysicsUpdate() {
        if (transformSaved) {
            body.setTransform(savedTransform);
            transformSaved = false;
        }

        super.postPhysicsUpdate();

        float maxForwardSpeed = engine.getMaxForwardSpeed();
        float maxForwardSpeedSquared = maxForwardSpeed * maxForwardSpeed;
        double magnitudeSquared = body.getLinearVelocity().getMagnitudeSquared();
        if (magnitudeSquared > maxForwardSpeedSquared) {
            double percent = maxForwardSpeedSquared / magnitudeSquared;
            body.getLinearVelocity().multiply(percent);
        }

        updateComponents();
    }

    @Override
    public void saveTransform(Transform transform) {
        this.savedTransform.set(transform);
        transformSaved = true;
    }

    private void shoot() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.shoot();
        }
    }

    private void updateComponents() {
        if (shield != null) shield.update();
        if (armor != null) armor.update();
        if (reactor != null) reactor.update();
        if (hull != null) hull.update();

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.update();
        }

        if (world.isRemote()) {
            size = damages.size();
            for (int i = 0; i < size; i++) {
                Damage damage = damages.get(i);
                damage.update();
            }
        }
    }

    private void damageByCollision(Ship otherShip, float impactPower, Contact contact, Vector2 normal) {
        if (collisionTimer > 0) {
            return;
        }

        if (world.isRemote()) {
            Random rand = world.getRand();
            ParticleSpawner.spawnDirectedSpark(contact, normal, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
            Vector2f angletovel = RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.15f);
            Vector2 point = contact.getPoint();
            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), (float) point.x, (float) point.y, velocity.x * 0.25f + angletovel.x, velocity.y * 0.25f + angletovel.y, 2.0f * rand.nextFloat());
        }

        lastAttacker = otherShip;
        if (otherShip.faction == faction) {
            impactPower /= 2.0f;
        }

        collisionTimer = 2;

        if (shield != null && shield.damage(impactPower)) {
            if (world.isRemote()) {
                Vector4f color = shield.getColor();
                ParticleSpawner.spawnDirectedSpark(contact, normal, 4.5f, color.x, color.y, color.z, color.w);
            }
            return;
        }

        float hullDamage = impactPower;
        float armorDamage = impactPower;
        Vector2f otherPos = otherShip.getPosition();
        Direction dir = calculateDirectionToOtherObject(otherPos.x, otherPos.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
    }

    public boolean attackShip(BulletDamage damage, Ship attacker, Vector2f contactPoint, float multiplayer) {
        lastAttacker = attacker;
        float shieldDamage = damage.getBulletDamageShield() * multiplayer;

        if ((shield != null && shield.damage(shieldDamage))) {
            return false;
        }

        float hullDamage = damage.getBulletDamageHull() * multiplayer;
        float armorDamage = damage.getBulletDamageArmor() * multiplayer;
        Direction dir = calculateDirectionToOtherObject(contactPoint.x, contactPoint.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);

        return true;
    }

    public void setDestroing() {
        if (destroingTimer == 0) destroingTimer = maxDestroingTimer;
    }

    public boolean isDestroing() {
        return destroingTimer != 0;
    }

    private void createSpark() {
        Random rand = world.getRand();
        Vector2f position = getPosition();
        Vector2f velocity = getVelocity();
        float randomVectorX = position.x + -scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f));
        float randomVectorY = position.y + -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f));
        if (world.isRemote()) {
            float baseSize = 4.0f + scale.x * 0.25f;
            ParticleSpawner.spawnMediumGarbage(rand.nextInt(2) + 1, randomVectorX, randomVectorY, velocity.x * 0.02f, velocity.y * 0.02f, baseSize - rand.nextFloat() * 2.5f);
            ParticleSpawner.spawnSmallGarbage(4, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)), position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)),
                    velocity.x * 0.001f, velocity.y * 0.001f, baseSize);
            ParticleSpawner.spawnShipOst(1 + rand.nextInt(3), randomVectorX, randomVectorY, velocity.x * 0.02f, velocity.y * 0.02f, 1.0f);
            ParticleSpawner.spawnLight(randomVectorX, randomVectorY, baseSize + rand.nextFloat() * 2.0f, 60.0f, 1.0f, 0.5f, 0.5f, 0.7f, 0.03f * 60.0f, false, RenderLayer.DEFAULT_ADDITIVE);
            ParticleSpawner.spawnSpark(randomVectorX, randomVectorY, baseSize + rand.nextFloat() * 2.0f);
            ParticleSpawner.spawnExplosion(randomVectorX, randomVectorY, baseSize + rand.nextFloat() * 2.0f);
            Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, randomVectorX, randomVectorY));
        } else {
            ParticleSpawner.spawnDamageDebris(world, 1, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)), position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)),
                    velocity.x * 0.1f, velocity.y * 0.1f, 1.0f);
        }
    }

    protected abstract void createDestroyParticles();

    public void destroyShip() {
        createDestroyParticles();
        if (!world.isRemote()) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
            setDead(true);
        }
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
            InstancedRenderer.INSTANCE.addToRenderPipeLine(lastJumpPosition.x, lastJumpPosition.y, jumpPosition.x, jumpPosition.y, rotation, size, size,
                    effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, jumpTexture, BufferType.ENTITIES_ADDITIVE);
        }
    }

    public void render() {
        if (spawned) {
            Vector2f position = getPosition();

            InstancedRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                    color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);

            if (hull.getHull() < hull.getMaxHull()) {
                float hp = hull.getHull() / hull.getMaxHull();
                InstancedRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
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

    @Override
    public void renderDebug() {
        if (spawned) super.renderDebug();
    }

    private void renderGunSlots() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.render();
        }
    }

    private void renderGunSlotsAdditive() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.renderAdditive();
        }
    }

    private void renderShield() {
        if (shield != null) {
            shield.render();
        }
    }

    public void setHull(Hull hull) {
        this.hull = hull;
    }

    protected void setCrew(Crew crew) {
        this.crew = crew;
    }

    protected void createWeaponPosition(Vector2f pos) {
        weaponPositions.add(pos);
    }

    private Vector2f getWeaponSlotPosition(int i) {
        return weaponPositions.get(i);
    }

    protected void setWeaponsCount(int count) {
        weaponSlots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            weaponSlots.add(null);
        }
    }

    public void addWeaponToSlot(int i, WeaponSlot slot) {
        if (i < weaponSlots.size()) {
            WeaponSlot oldSlot = weaponSlots.get(i);
            if (oldSlot != null) {
                oldSlot.clear();
            }
        }

        slot.init(i, getWeaponSlotPosition(i), this);

        weaponSlots.set(i, slot);

        if (!world.isRemote()) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipSetWeaponSlot(this, slot), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public void recalculateMass() {
        body.setMass(MassType.NORMAL);
    }

    public WeaponSlot getWeaponSlot(int i) {
        return weaponSlots.get(i);
    }

    public void setReactor(Reactor reactor) {
        this.reactor = reactor;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public void setShield(Shield shield) {
        this.shield = shield;
    }

    public abstract void spawnEngineParticles(Direction dir);

    public void setSpawned() {
        color.w = 1.0f;
        spawned = true;
        world.spawnShip(this);
        createName();
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void setVelocity(Vector2f velocity) {
        body.setLinearVelocity(new Vector2(velocity.x, velocity.y));
    }

    public void setRotation(float rotation) {
        body.getTransform().setRotation(rotation);
    }

    public Vector3f getEffectsColor() {
        return effectsColor;
    }

    public void clear() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            if (slot instanceof WeaponSlotBeam) slot.clear();
        }
    }

    private void createName() {
        if (world.isRemote() && name != null) {
            stringObject = new StringObject(FontType.XOLONIUM, name, 14, StringOffsetType.CENTERED);
            stringObject.compile();
        }
    }

    public void setName(String name) {
        this.name = name;
        if (world.isRemote()) {
            if (spawned) createName();
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipName(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
        if (!world.isRemote()) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipFaction(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public void addDamage(Damage d) {
        damages.add(d);
    }

    public boolean isBot() {
        return owner == null;
    }

    public void setControlledByPlayer(boolean controlledByPlayer) {
        this.controlledByPlayer = controlledByPlayer;
    }

    public boolean isControlledByPlayer() {
        return controlledByPlayer;
    }

    public void setTarget(CollisionObject target) {
        this.target = target;
    }

    public void setMoveDirection(Direction dir) {
        remoteMoveDirectionForEngineParticles = dir;
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);

        sin = (float) body.getTransform().getRotation().getSint();
        cos = (float) body.getTransform().getRotation().getCost();

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.updatePos();
        }

        size = damages.size();
        for (int i = 0; i < size; i++) {
            Damage damage = damages.get(i);
            damage.updatePos();
        }
    }

    @Override
    public void updateServerPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateServerPositionFromPacket(pos, rot, velocity, angularVelocity);

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.updatePos();
        }
    }

    public abstract ShipType getType();
}