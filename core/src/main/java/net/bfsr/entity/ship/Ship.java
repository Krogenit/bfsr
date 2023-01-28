package net.bfsr.entity.ship;

import net.bfsr.ai.Ai;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.ai.task.AiAttackTarget;
import net.bfsr.ai.task.AiSearchTarget;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringOffsetType;
import net.bfsr.client.render.font.string.StaticString;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
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
import net.bfsr.math.RotationHelper;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.*;
import net.bfsr.server.MainServer;
import net.bfsr.settings.EnumOption;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Ship extends CollisionObject {
    private Armor armor;
    private Shield shield;
    private Engine engine;
    private Faction faction;
    private Reactor reactor;
    private Crew crew;
    private Hull hull;
    private Cargo cargo;

    private String name;
    private StringObject stringObject;

    private final List<Vector2f> weaponPositions = new ArrayList<>();
    private List<WeaponSlot> weaponSlots;
    private float sin, cos;
    private boolean spawned;
    private final Vector2f jumpVelocity;
    private final Vector2f jumpPosition;
    private final Vector2f lastJumpPosition = new Vector2f();
    private final float jumpSpeed = 25.0f;
    private final Vector3f effectsColor;
    private int collisionTimer;
    private int destroingTimer, sparksTimer;
    protected int maxDestroingTimer, maxSparksTimer;

    private PlayerServer owner;
    private boolean controlledByPlayer;

    private Ai ai;
    private CollisionObject lastAttacker, target;

    protected Texture textureDamage;
    private List<Damage> damages;

    private Direction remoteMoveDirectionForEngineParticles;

    protected Ship(WorldServer world, Vector2f pos, float rot, Vector2f scale, Vector3f effectsColor, boolean spawned) {
        super(world, world.getNextId(), pos, scale);
        color = new Vector4f(1, 1, 1, 0);

        rotate = rot;
        jumpVelocity = RotationHelper.angleToVelocity(rotate + Math.PI, -jumpSpeed * 6.0f);
        jumpPosition = new Vector2f(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + pos.x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + pos.y);
        position = new Vector2f(pos);
        this.effectsColor = effectsColor;
        setRotation(rotate);
        if (spawned) setSpawmed();
        ai = new Ai(this);
        ai.setAggressiveType(AiAggressiveType.ATTACK);
        ai.addTask(new AiSearchTarget(this, 4000.0f));
        ai.addTask(new AiAttackTarget(this, 4000.0f));
        init();
        this.world.addShip(this);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    protected Ship(WorldClient w, int id, TextureRegister texture, Vector2f pos, float rot, Vector2f scale, Vector3f effectsColor) {
        super(w, id, texture, pos, scale);
        color = new Vector4f(1, 1, 1, 0);

        damages = new ArrayList<>();
        rotate = rot;
        jumpVelocity = RotationHelper.angleToVelocity(rotate + Math.PI, -jumpSpeed * 6.0f);
        jumpPosition = new Vector2f(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + pos.x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + pos.y);
        position = new Vector2f(pos);
        this.effectsColor = effectsColor;
        setRotation(rotate);
        ai = new Ai(this);
        init();
        world.addShip(this);
    }

    protected abstract void init();

    public void control() {
        if (destroingTimer == 0) {
            if (body.isAtRest()) body.setAtRest(false);

            rotateToVector(Mouse.getWorldPosition(Core.getCore().getRenderer().getCamera()), engine.getRotationSpeed());

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
                Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion1, getPosition()));
                ParticleSpawner.spawnShockwave(0, getPosition(), baseSize + 3.0f);
                for (int i = 0; i < 8; i++)
                    ParticleSpawner.spawnMediumGarbage(1, getPosition().add(new Vector2f(-scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f)), -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f)))),
                            new Vector2f(getVelocity()).add(RotationHelper.angleToVelocity(rand.nextFloat() * RotationHelper.TWOPI, scale.x)),
                            baseSize);
                float size = (scale.x + scale.y) * 1.1f;
                ParticleSpawner.spawnSpark(getPosition(), size);
                ParticleSpawner.spawnLight(getPosition(), size, 4.0f * 6.0f, new Vector4f(1, 0.5f, 0.4f, 1.0f), 0.05f * 60.0f, true, EnumParticlePositionType.Default);
                ParticleSpawner.spawnRocketShoot(getPosition(), size);
            }
        }
    }

    private void updateShip() {
        Vector2f position = getPosition();
        lastPosition.set(position.x, position.y);

        if (world.isRemote()) {
            if (this == world.getPlayerShip()) {
                Core.getCore().sendPacket(new PacketObjectPosition(this));
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
            Vector2f pos = position;
            if (controlledByPlayer) {
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearbyExcept(new PacketObjectPosition(this), pos, WorldServer.PACKET_SPAWN_DISTANCE, player);
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), pos, WorldServer.PACKET_UPDATE_DISTANCE);
            } else {
                if (destroingTimer == 0 && ai != null) ai.update();

                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketObjectPosition(this), pos, WorldServer.PACKET_SPAWN_DISTANCE);
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), pos, WorldServer.PACKET_UPDATE_DISTANCE);
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
            } else if (userData instanceof ParticleWreck) {
                if (collisionTimer <= 0) {
                    collisionTimer = 2;
                    if (world.isRemote()) {
                        if (shield != null) {
                            Vector4f color = shield.getColor();
                            ParticleSpawner.spawnDirectedSpark(contact, normal, 4.5f, new Vector4f(color));
                        } else {
                            ParticleSpawner.spawnDirectedSpark(contact, normal, 3.75f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
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
            if (((jumpVelocity.x < 0 && jumpPosition.x <= position.x) || (jumpVelocity.x > 0 && jumpPosition.x >= position.x)) && ((jumpVelocity.y < 0 && jumpPosition.y <= position.y)
                    || (jumpVelocity.y > 0 && jumpPosition.y >= position.y))) {
                setSpawmed();
                setVelocity(jumpVelocity.mul(0.26666668f));
                if (world.isRemote()) {
                    ParticleSpawner.spawnLight(position, new Vector2f(getVelocity()).mul(0.5f), 32.0f + scale.x * 0.25f, new Vector4f(effectsColor.x, effectsColor.y, effectsColor.z, 1.0f), 3.6f,
                            true, EnumParticlePositionType.Default);
                    ParticleSpawner.spawnDisableShield(position, new Vector2f(getVelocity()).mul(0.5f), 32.0f + scale.x * 0.25f, new Vector4f(effectsColor.x, effectsColor.y, effectsColor.z, 1.0f));
                    Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.jump, position));
                }
            } else {
                lastJumpPosition.set(jumpPosition);
                jumpPosition.add(jumpVelocity.x * TimeUtils.UPDATE_DELTA_TIME, jumpVelocity.y * TimeUtils.UPDATE_DELTA_TIME);
                color.w += 1.5f * TimeUtils.UPDATE_DELTA_TIME;
            }
        }
    }

    public void postPhysicsUpdate() {
        float maxForwardSpeed = engine.getMaxForwardSpeed();
        float maxForwardSpeedSquared = maxForwardSpeed * maxForwardSpeed;
        double magnitudeSquared = body.getLinearVelocity().getMagnitudeSquared();
        if (magnitudeSquared > maxForwardSpeedSquared) {
            double percent = maxForwardSpeedSquared / magnitudeSquared;
            body.getLinearVelocity().multiply(percent);
        }

        double rotation = getRotation();
        sin = (float) Math.sin(rotation);
        cos = (float) Math.cos(rotation);

        updateComponents();

        if (stringObject != null) {
            Transform transform = body.getTransform();
            stringObject.setPosition((int) transform.getTranslationX(), (int) (transform.getTranslationY() + 3.2f + scale.y / 4.0f));
        }
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
            ParticleSpawner.spawnDirectedSpark(contact, normal, 3.75f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
            Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.15f);
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
                ParticleSpawner.spawnDirectedSpark(contact, normal, 4.5f, new Vector4f(color));
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
        Vector2f randomVector1 = new Vector2f(position).add(-scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f)), -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f)));
        if (world.isRemote()) {
            float baseSize = 4.0f + scale.x * 0.25f;
            ParticleSpawner.spawnMediumGarbage(rand.nextInt(2) + 1, randomVector1, new Vector2f(getVelocity()).mul(0.02f), baseSize - rand.nextFloat() * 2.5f);
            ParticleSpawner.spawnSmallGarbage(4, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)), position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)), velocity.x * 0.001f, velocity.y * 0.001f, baseSize);
            ParticleSpawner.spawnShipOst(1 + rand.nextInt(3), randomVector1, new Vector2f(getVelocity()).mul(0.02f), 1.0f);
            ParticleSpawner.spawnLight(randomVector1, baseSize + rand.nextFloat() * 2.0f, 60.0f, new Vector4f(1.0f, 0.5f, 0.5f, 0.7f), 0.03f * 60.0f, false, EnumParticlePositionType.Default);
            ParticleSpawner.spawnSpark(randomVector1, baseSize + rand.nextFloat() * 2.0f);
            ParticleSpawner.spawnExplosion(randomVector1, baseSize + rand.nextFloat() * 2.0f);
            Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, randomVector1));
        } else {
            ParticleSpawner.spawnDamageDerbis(world, 1, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)), position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)), velocity.x * 0.1f, velocity.y * 0.1f, 1.0f);
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

    public void renderTransparent(BaseShader shader, float interpolation) {
        if (spawned) {
            int size = damages.size();
            for (int i = 0; i < size; i++) {
                Damage damage = damages.get(i);
                damage.renderEffects(interpolation);
            }

            renderShield(shader, interpolation);
        } else {
            InstancedRenderer.INSTANCE.addToRenderPipeLine(Transformation.getDefaultModelMatrix(lastJumpPosition.x, lastJumpPosition.y, jumpPosition.x, jumpPosition.y, rotate, 40.0f * color.w,
                    40.0f * color.w, interpolation), effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, TextureLoader.getTexture(TextureRegister.particleJump));
        }
    }

    public void render(BaseShader shader, float interpolation) {
        if (spawned) {
            FloatBuffer modelMatrix = Transformation.getModelMatrix(this, interpolation);
            InstancedRenderer.INSTANCE.addToRenderPipeLine(modelMatrix, color.x, color.y, color.z, color.w, texture);

            if (hull.getHull() < hull.getMaxHull()) {
                float hp = hull.getHull() / hull.getMaxHull();
                InstancedRenderer.INSTANCE.addToRenderPipeLine(modelMatrix, 1.0f, 1.0f, 1.0f, 1.0f - hp, textureDamage);
            }

            int size = damages.size();
            for (int i = 0; i < size; i++) {
                Damage damage = damages.get(i);
                damage.render(shader, interpolation);
            }

            renderGunSlots(interpolation);
        }
    }

    @Override
    public void renderDebug() {
        if (spawned) super.renderDebug();
    }

    private void renderGunSlots(float interpolation) {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) InstancedRenderer.INSTANCE.addToRenderPipeLine(weaponSlot, interpolation);
        }
    }

    private void renderShield(BaseShader shader, float interpolation) {
        if (shield != null) {
            shield.render(shader, interpolation);
        }
    }

    public void setHull(Hull hull) {
        this.hull = hull;
    }

    public Hull getHull() {
        return hull;
    }

    public Crew getCrew() {
        return crew;
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

    public int getWeaponSlotId(WeaponSlot weaponSlot) {
        return weaponSlots.indexOf(weaponSlot);
    }

    public void addWeaponToSlot(int i, WeaponSlot slot) {
        if (i < weaponSlots.size()) {
            WeaponSlot oldSlot = weaponSlots.get(i);
            if (oldSlot != null) {
                oldSlot.clear();
            }
        }

        slot.setAddPosition(getWeaponSlotPosition(i));
        slot.setShip(this);
        slot.createBody();
        slot.setId(i);

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

    public List<WeaponSlot> getWeaponSlots() {
        return weaponSlots;
    }

    public void setReactor(Reactor reactor) {
        this.reactor = reactor;
    }

    public Reactor getReactor() {
        return reactor;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public Armor getArmor() {
        return armor;
    }

    public void setShield(Shield shield) {
        this.shield = shield;
    }

    public Shield getShield() {
        return shield;
    }

    public float getSin() {
        return sin;
    }

    public float getCos() {
        return cos;
    }

    public abstract void spawnEngineParticles(Direction dir);

    public void setSpawmed() {
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

    public void setRotation(float rotate) {
        body.getTransform().setRotation(rotate);
    }

    public Vector3f getEffectsColor() {
        return effectsColor;
    }

    public String getName() {
        return name;
    }

    public void clear() {
        if (stringObject != null) {
            stringObject.clear();
        }

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            if (slot instanceof WeaponSlotBeam) slot.clear();
        }
    }

    private void createName() {
        if (world.isRemote() && name != null) {
            clear();
            stringObject = new StaticString(FontType.XOLONIUM, name, 20, StringOffsetType.CENTERED);
            stringObject.compile();
            Transform transform = body.getTransform();
            stringObject.setPosition((int) transform.getTranslationX(), (int) (transform.getTranslationY() + 3.2f + scale.y / 4.0f));
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

    public List<Damage> getDamages() {
        return damages;
    }

    public Faction getFaction() {
        return faction;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public CollisionObject getLastAttacker() {
        return lastAttacker;
    }

    public PlayerServer getOwner() {
        return owner;
    }

    public void setOwner(PlayerServer owner) {
        this.owner = owner;
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

    public CollisionObject getTarget() {
        return target;
    }

    public void setTarget(CollisionObject target) {
        this.target = target;
    }

    public Ai getAi() {
        return ai;
    }

    public void setAi(Ai ai) {
        this.ai = ai;
    }

    public abstract TextureRegister getWreckTexture(int textureOffset);

    public abstract TextureRegister getWreckFireTexture(int textureOffset);

    public abstract TextureRegister getWreckLightTexture(int textureOffset);

    public void setMoveDirection(Direction dir) {
        remoteMoveDirectionForEngineParticles = dir;
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);

        double rotation = getRotation();
        sin = (float) Math.sin(rotation);
        cos = (float) Math.cos(rotation);

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

        if (stringObject != null) {
            Transform transform = body.getTransform();
            stringObject.setPosition((int) transform.getTranslationX(), (int) (transform.getTranslationY() + 32.0f + scale.y / 4.0f));
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
}