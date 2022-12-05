package net.bfsr.entity.ship;

import net.bfsr.ai.Ai;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.ai.task.AiAttackTarget;
import net.bfsr.ai.task.AiSearchTarget;
import net.bfsr.client.font.GUIText;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.texture.Texture;
import net.bfsr.client.texture.TextureRegister;
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
import net.bfsr.math.EnumZoomFactor;
import net.bfsr.math.RotationHelper;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.*;
import net.bfsr.server.MainServer;
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
import org.lwjgl.opengl.GL11;

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
    private GUIText textName;

    private final List<Vector2f> weaponPositions = new ArrayList<>();
    private List<WeaponSlot> weaponSlots;
    private float sin, cos;
    private boolean spawned;
    private final Vector2f jumpVelocity;
    private final Vector2f jumpPosition;
    private final float jumpSpeed = 25f;
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

    public Ship(WorldServer w, Vector2f pos, float rot, Vector2f scale, Vector3f effectsColor, boolean spawned) {
        super(w, w.getNextId(), pos, scale);
        this.color = new Vector4f(1, 1, 1, 0);

        this.rotate = rot;
        this.jumpVelocity = RotationHelper.angleToVelocity(rotate + Math.PI, -jumpSpeed * 60f);
        this.jumpPosition = new Vector2f(jumpVelocity.x / 60f * (64f + scale.x * 0.1f) * -0.5f + pos.x, jumpVelocity.y / 60f * (64f + scale.y * 0.1f) * -0.5f + pos.y);
        this.position = new Vector2f(pos);
        this.effectsColor = effectsColor;
        setRotation(rotate);
        if (spawned) setSpawmed();
        this.ai = new Ai(this);
        this.ai.setAggressiveType(AiAggressiveType.ATTACK);
        this.ai.addTask(new AiSearchTarget(this, 40000f));
        this.ai.addTask(new AiAttackTarget(this, 40000f));
        init();
        world.addShip(this);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public Ship(WorldClient w, int id, TextureRegister texture, Vector2f pos, float rot, Vector2f scale, Vector3f effectsColor) {
        super(w, id, texture, pos, scale);
        this.color = new Vector4f(1, 1, 1, 0);

        this.damages = new ArrayList<>();
        this.rotate = rot;
        this.jumpVelocity = RotationHelper.angleToVelocity(rotate + Math.PI, -jumpSpeed * 60f);
        this.jumpPosition = new Vector2f(jumpVelocity.x / 60f * (64f + scale.x * 0.1f) * -0.5f + pos.x, jumpVelocity.y / 60f * (64f + scale.y * 0.1f) * -0.5f + pos.y);
        this.position = new Vector2f(pos);
        this.effectsColor = effectsColor;
        setRotation(rotate);
        this.ai = new Ai(this);
//		this.ai.addTask(new AiSearchTarget(this, 1000f));
//		this.ai.addTask(new AiAttackTarget(this, 1000f));
        init();
        world.addShip(this);
    }

    protected abstract void init();

    protected void createBody(Vector2f pos) {
        super.createBody(pos);
    }

    public void control(double delta) {
        if (destroingTimer == 0) {
            if (body.isAtRest()) body.setAtRest(false);

            rotateToVector(Mouse.getWorldPosition(Core.getCore().getRenderer().getCamera()), engine.getRotationSpeed(), delta);

            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_W)) {
                engine.setMaxPower(true);
                move(this, delta, Direction.FORWARD);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_S)) {
                move(this, delta, Direction.BACKWARD);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_A)) {
                move(this, delta, Direction.LEFT);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_D)) {
                move(this, delta, Direction.RIGHT);
            }
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_X)) {
                move(this, delta, Direction.STOP);
            }

            if (Mouse.isLeftDown()) {
                shoot();
            }

            engine.setMaxPower(false);

            if (Core.getCore().getSettings().isDebug() && Keyboard.isKeyPressed(GLFW.GLFW_KEY_R)) {
//				createSpark();
                float baseSize = 40f + scale.x * 0.25f;
                Random rand = world.getRand();
                Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion1, getPosition()));
                ParticleSpawner.spawnShockwave(0, getPosition(), baseSize + 30f);
                for (int i = 0; i < 8; i++)
                    ParticleSpawner.spawnMediumGarbage(1, getPosition().add(new Vector2f(-scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f)), -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f)))),
                            new Vector2f(getVelocity()).add(RotationHelper.angleToVelocity(rand.nextFloat() * RotationHelper.TWOPI, scale.x)),
                            baseSize);
                float size = (scale.x + scale.y) * 1.1f;
                ParticleSpawner.spawnSpark(getPosition(), size);
                ParticleSpawner.spawnLight(getPosition(), size, 4f * 60f, new Vector4f(1, 0.5f, 0.4f, 1f), 0.05f * 60f, true, EnumParticlePositionType.Default);
                ParticleSpawner.spawnRocketShoot(getPosition(), size);
            }
        }
    }

    private void updateShip(double delta) {
        if (world.isRemote()) {
            if (this == world.getPlayerShip()) {
//				if(updateTimer <= 0) {
                Core.getCore().sendPacket(new PacketObjectPosition(this));
//					updateTimer = UPDATE_POS_TIMER;
//				} else {
//					updateTimer--;
//				}
                aliveTimer = 0;
            } else {
                if (remoteMoveDirectionForEngineParticles != null) {
                    spawnEngineParticles(remoteMoveDirectionForEngineParticles, delta);
                }
            }
        }

//		checkCollision(delta);
        if (collisionTimer > 0) collisionTimer -= 60f * delta;
        if (world.isRemote()) {
            if (destroingTimer > 0) {
                sparksTimer -= 60f * delta;
                if (sparksTimer <= 0) {
                    createSpark();
                    sparksTimer = 25;
                }
            }
        } else {
            WorldServer world = (WorldServer) this.world;
            PlayerServer player = world.getPlayer(getName());
            Vector2f pos = getPosition();
            if (controlledByPlayer) {
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearbyExcept(new PacketObjectPosition(this), pos, WorldServer.PACKET_SPAWN_DISTANCE, player);
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), pos, WorldServer.PACKET_UPDATE_DISTANCE);
            } else {
                if (destroingTimer == 0 && ai != null) ai.update(delta);

//				if(--updateTimer <= 0) {
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketObjectPosition(this), pos, WorldServer.PACKET_SPAWN_DISTANCE);
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), pos, WorldServer.PACKET_UPDATE_DISTANCE);
//					updateTimer = UPDATE_POS_TIMER;
//				}
            }

            if (destroingTimer > 0) {
                sparksTimer -= 60f * delta;
                if (sparksTimer <= 0) {
                    createSpark();
                    sparksTimer = 25;
                }

                destroingTimer -= 60f * delta;
                if (destroingTimer <= 0) {
                    destroyShip();
                }
            }
        }
    }

    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship) {
                Ship otherShip = (Ship) userData;

                Vector2f velocityDif = new Vector2f((float) (body.getLinearVelocity().x - this.body.getLinearVelocity().x),
                        (float) (body.getLinearVelocity().y - this.body.getLinearVelocity().y));
                float impactPowerForOther = (float) ((velocityDif.length()) *
                        (this.body.getMass().getMass() / body.getMass().getMass()));

                impactPowerForOther /= 400f;

                if (impactPowerForOther > 0.25f) otherShip.damageByCollision(this, impactPowerForOther, contact, normal);
            } else if (userData instanceof ParticleWreck) {
                if (collisionTimer <= 0) {
                    collisionTimer = 2;
                    if (world.isRemote()) {
                        if (shield != null) {
                            Vector4f color = shield.getColor();
                            ParticleSpawner.spawnDirectedSpark(contact, normal, 45f, new Vector4f(color));
                        } else {
                            ParticleSpawner.spawnDirectedSpark(contact, normal, 37.5f, new Vector4f(1f, 1f, 1f, 1f));
                        }
                    }
                }
            }
        }
    }

    public void update(double delta) {
        super.update(delta);

        if (spawned) {
            updateShip(delta);
        } else {
            if (((jumpVelocity.x < 0 && jumpPosition.x <= position.x)
                    || (jumpVelocity.x > 0 && jumpPosition.x >= position.x))
                    && ((jumpVelocity.y < 0 && jumpPosition.y <= position.y)
                    || (jumpVelocity.y > 0 && jumpPosition.y >= position.y))) {
                setSpawmed();
                setVelocity(jumpVelocity.mul(8f / 30f));
                if (world.isRemote()) {
                    ParticleSpawner.spawnLight(position, new Vector2f(getVelocity()).mul(0.02f), 64f * 5f + scale.x * 0.25f, new Vector4f(effectsColor.x, effectsColor.y, effectsColor.z, 1f), 0.06f * 60f, true, EnumParticlePositionType.Default);
                    ParticleSpawner.spawnDisableShield(position, getVelocity(), 64f * 5f + scale.x * 0.25f, new Vector4f(effectsColor.x, effectsColor.y, effectsColor.z, 1f));
                    Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.jump, position));
                }
            } else {
                jumpPosition.add(jumpVelocity.x * (float) delta, jumpVelocity.y * (float) delta);
                color.w += 1.5f * delta;
            }
        }
    }

    public void postPhysicsUpdate(double delta) {
        double rotation = getRotation();
        sin = (float) Math.sin(rotation);
        cos = (float) Math.cos(rotation);

        updateComponents(delta);

        if (textName != null) {
            Transform transform = body.getTransform();
            textName.setPosition((float) transform.getTranslationX(), (float) transform.getTranslationY() + 32f + scale.y / 4f);
        }
    }

    private void shoot() {
        for (WeaponSlot weaponSlot : weaponSlots) {
            if (weaponSlot != null) weaponSlot.shoot();
        }
    }

    private void updateComponents(double delta) {
        if (shield != null) shield.update(delta);
        if (armor != null) armor.update(delta);
        if (reactor != null) reactor.update(delta);
        if (hull != null) hull.update(delta);

        for (WeaponSlot weaponSlot : weaponSlots) {
            if (weaponSlot != null) weaponSlot.update(delta);
        }

        if (world.isRemote()) {
            for (Damage damage : damages) {
                damage.update(delta);
            }
        }
    }

    public boolean damageByCollision(Ship otherShip, float impactPower, Contact contact, Vector2 normal) {
        if (collisionTimer > 0) {
            return false;
        }

        if (world.isRemote()) {
            Random rand = world.getRand();
            ParticleSpawner.spawnDirectedSpark(contact, normal, 37.5f, new Vector4f(1f, 1f, 1f, 1f));
            Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
            Vector2 point = contact.getPoint();
            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), (float) point.x, (float) point.y, velocity.x * 0.25f + angletovel.x, velocity.y * 0.25f + angletovel.y, 20f * rand.nextFloat());
        }

//		if(otherShip.getFaction() != faction) {
        lastAttacker = otherShip;
        if (otherShip.getFaction() == faction) {
            impactPower /= 2f;
        }

        collisionTimer = 2;
        //TODO: нельзя удалять щит на стадии обработки контактов ContactListener ConcurrentModificationException AbstractCollisionWorld.java:1215
        if (shield != null && shield.damage(impactPower)) {
            if (world.isRemote()) {
                Vector4f color = shield.getColor();
                ParticleSpawner.spawnDirectedSpark(contact, normal, 45f, new Vector4f(color));
            }
            return false;
        }

        float hullDamage = impactPower;
        float armorDamage = impactPower;
        Vector2f otherPos = otherShip.getPosition();
        Direction dir = calculateDirectionToOtherObject(otherPos.x, otherPos.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        return true;
//		}

//		return false;
    }

    public boolean attackShip(BulletDamage damage, Ship attacker, Vector2f contactPoint, float multiplayer) {
//		if(true) return false;
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
            float baseSize = 40f + scale.x * 0.25f;
            ParticleSpawner.spawnMediumGarbage(rand.nextInt(2) + 1, randomVector1, new Vector2f(getVelocity()).mul(0.02f), baseSize - rand.nextFloat() * 25f);
            ParticleSpawner.spawnSmallGarbage(4, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)), position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)), velocity.x * 0.001f, velocity.y * 0.001f, baseSize);
            ParticleSpawner.spawnShipOst(1 + rand.nextInt(3), randomVector1, new Vector2f(getVelocity()).mul(0.02f), 1f);
            ParticleSpawner.spawnLight(randomVector1, baseSize + rand.nextFloat() * 20f, 600f, new Vector4f(1.0f, 0.5f, 0.5f, 0.7f), 0.03f * 60f, false, EnumParticlePositionType.Default);
            ParticleSpawner.spawnSpark(randomVector1, baseSize + rand.nextFloat() * 20f);
            ParticleSpawner.spawnExplosion(randomVector1, baseSize + rand.nextFloat() * 20f);
            Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, randomVector1));
        } else {
            ParticleSpawner.spawnDamageDerbis(world, 1, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)), position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)), velocity.x * 0.1f, velocity.y * 0.1f, 1f);
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

    public void render(BaseShader shader) {
        if (spawned) {
            OpenGLHelper.alphaGreater(0.75f);
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            super.render(shader);
            shader.setColor(new Vector4f(effectsColor, 1.0f));
            OpenGLHelper.bindTexture(textureDamage.getId());
            Vector2f pos = getPosition();
            float hp = hull.getHull() / hull.getMaxHull();
            OpenGLHelper.alphaGreater(0.001f);
            shader.setColor(new Vector4f(1f, 1f, 1f, 1f - hp));
            shader.setModelViewMatrix(Transformation.getModelViewMatrix(pos.x, pos.y, getRotation(),
                    scale.x, scale.y, EnumZoomFactor.Default));
            Renderer.quad.render();
            for (Damage damage : damages) {
                damage.render(shader);
            }
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            for (Damage damage : damages) {
                damage.renderEffects(shader);
            }
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            OpenGLHelper.alphaGreater(0.75f);
            renderGunSlots(shader);
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            renderShield(shader);
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            OpenGLHelper.alphaGreater(0.01f);
            shader.setColor(new Vector4f(effectsColor, 1.0f));
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            shader.enableTexture();
            OpenGLHelper.bindTexture(TextureLoader.getTexture(TextureRegister.particleJump).getId());
            shader.setModelViewMatrix(Transformation.getModelViewMatrix(jumpPosition.x, jumpPosition.y, rotate,
                    (400f) * color.w, (400f) * color.w, EnumZoomFactor.Default));
            Renderer.quad.render();
        }
    }

    @Override
    public void renderDebug() {
        if (spawned) super.renderDebug();
    }

    private void renderGunSlots(BaseShader shader) {
        for (WeaponSlot weaponSlot : weaponSlots) {
            if (weaponSlot != null) weaponSlot.render(shader);
        }
    }

    private void renderShield(BaseShader shader) {
        if (shield != null) {
            shield.render(shader);
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

    public void setCrew(Crew crew) {
        this.crew = crew;
    }

    public void createWeaponPosition(Vector2f pos) {
        this.weaponPositions.add(pos);
    }

    public Vector2f getWeaponSlotPosition(int i) {
        return this.weaponPositions.get(i);
    }

    public void setWeapoinsCount(int count) {
        this.weaponSlots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.weaponSlots.add(null);
        }
    }

    public int getWeaponSlotId(WeaponSlot weaponSlot) {
        return this.weaponSlots.indexOf(weaponSlot);
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

        this.weaponSlots.set(i, slot);

        if (!world.isRemote()) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipSetWeaponSlot(this, slot), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public void recalculateMass() {
        body.setMass(MassType.NORMAL);
    }

    public WeaponSlot getWeaponSlot(int i) {
        return this.weaponSlots.get(i);
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

    public abstract void spawnEngineParticles(Direction dir, double delta);

    public void setSpawmed() {
        this.color.w = 1.0f;
        this.spawned = true;
        this.world.spawnShip(this);
        createName();
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void setVelocity(Vector2f velocity) {
        this.body.setLinearVelocity(new Vector2(velocity.x, velocity.y));
    }

    public void setRotation(float rotate) {
        this.body.getTransform().setRotation(rotate);
    }

    public float getSpawnRotation() {
        return rotate;
    }

    public Vector3f getEffectsColor() {
        return effectsColor;
    }

    public GUIText getTextName() {
        return textName;
    }

    public String getName() {
        return name;
    }

    public void clear() {
        if (textName != null) {
            textName.clear();
        }

        for (WeaponSlot slot : weaponSlots) {
            if (slot instanceof WeaponSlotBeam) slot.clear();
        }
    }

    private void createName() {
        if (world.isRemote() && name != null) {
            clear();
            this.textName = new GUIText(name, new Vector2f(0.45f + scale.x / 400f, 0.45f + scale.x / 400f), new Vector2f(getPosition()), new Vector4f(1f, 1f, 1f, 1f), true, EnumParticlePositionType.Default);
            this.textName.setZoomFactor(EnumZoomFactor.Default);
            Transform transform = body.getTransform();
            this.textName.setPosition((float) transform.getTranslationX(), (float) transform.getTranslationY() + 32f + scale.y / 4f);
        }
    }

    public void setName(String name) {
        this.name = name;
        if (world.isRemote()) {
            if (isSpawned()) createName();
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
        this.damages.add(d);
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
        this.remoteMoveDirectionForEngineParticles = dir;
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);

        double rotation = getRotation();
        sin = (float) Math.sin(rotation);
        cos = (float) Math.cos(rotation);

        for (WeaponSlot weaponSlot : weaponSlots) {
            if (weaponSlot != null) weaponSlot.updatePos();
        }

        for (Damage damage : damages) {
            damage.updatePos();
        }

        if (textName != null) {
            Transform transform = body.getTransform();
            textName.setPosition((float) transform.getTranslationX(), (float) transform.getTranslationY() + 32f + scale.y / 4f);
        }
    }

    @Override
    public void updateServerPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        super.updateServerPositionFromPacket(pos, rot, velocity, angularVelocity);

        for (WeaponSlot weaponSlot : weaponSlots) {
            if (weaponSlot != null) weaponSlot.updatePos();
        }
    }
}