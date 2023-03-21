package net.bfsr.client.entity.ship;

import clipper2.core.PathsD;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.collision.filter.ShipFilter;
import net.bfsr.client.component.Damage;
import net.bfsr.client.component.Shield;
import net.bfsr.client.component.weapon.WeaponSlot;
import net.bfsr.client.core.Core;
import net.bfsr.client.damage.Damagable;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.entity.wreck.Wreck;
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
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.settings.Option;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.Armor;
import net.bfsr.component.Engine;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.ShipType;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.CollisionObjectUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Ship extends CollisionObject implements Damagable {
    private static final Texture JUMP_TEXTURE = TextureLoader.getTexture(TextureRegister.particleJump);

    @Getter
    @Setter
    private Armor armor;
    @Getter
    @Setter
    protected Shield shield;
    @Getter
    @Setter
    protected Engine engine;
    @Getter
    @Setter
    private Faction faction;
    @Getter
    @Setter
    private Reactor reactor;
    @Getter
    private Crew crew;
    @Getter
    protected Hull hull;
    @Getter
    @Setter
    private Cargo cargo;

    @Getter
    protected String name;
    private final StringObject stringObject = new StringObject(FontType.XOLONIUM, 14, StringOffsetType.CENTERED);

    private final List<Vector2f> weaponPositions = new ArrayList<>();
    @Getter
    protected List<WeaponSlot> weaponSlots;
    @Getter
    protected boolean spawned;
    protected final Vector2f jumpVelocity = new Vector2f();
    protected final Vector2f jumpPosition = new Vector2f();
    protected final Vector2f lastJumpPosition = new Vector2f();
    protected final float jumpSpeed = 25.0f;
    @Getter
    protected final Vector3f effectsColor = new Vector3f();
    private int collisionTimer;
    protected int destroyingTimer;
    protected int sparksTimer;
    protected int maxDestroyingTimer, maxSparksTimer;

    @Getter
    private CollisionObject lastAttacker;

    protected Texture textureDamage;
    private final List<Damage> damages;

    private Direction remoteMoveDirectionForEngineParticles;
    @Getter
    @Setter
    protected PathsD contours = new PathsD();
    @Getter
    protected DamageMaskTexture maskTexture;
    @Getter
    protected List<BodyFixture> fixturesToAdd = new ArrayList<>();

    protected Ship(WorldClient world, int id, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b,
                   TextureRegister texture, TextureRegister textureDamage) {
        super(world, id, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 0.0f, TextureLoader.getTexture(texture));
        this.textureDamage = TextureLoader.getTexture(textureDamage);
        this.damages = new ArrayList<>();
        RotationHelper.angleToVelocity(this.rotation + MathUtils.PI, -jumpSpeed * 6.0f, jumpVelocity);
        this.jumpPosition.set(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + y);
        this.effectsColor.set(r, g, b);
        setRotation(this.rotation);
        world.addShip(this);
    }

    public void control() {
        if (destroyingTimer == 0) {
            if (body.isAtRest()) body.setAtRest(false);

            CollisionObjectUtils.rotateToVector(this, Mouse.getWorldPosition(Core.get().getRenderer().getCamera()), engine.getRotationSpeed());

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
                    RotationHelper.angleToVelocity(rand.nextFloat() * MathUtils.TWO_PI, scale.x, CollisionObjectUtils.ANGLE_TO_VELOCITY);
                    ParticleSpawner.spawnMediumGarbage(1, position.x + -scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f)),
                            position.y + -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f)),
                            velocity.x + CollisionObjectUtils.ANGLE_TO_VELOCITY.x, velocity.y + CollisionObjectUtils.ANGLE_TO_VELOCITY.y, baseSize);
                }
                float size = (scale.x + scale.y) * 1.1f;
                ParticleSpawner.spawnSpark(position.x, position.y, size);
                ParticleSpawner.spawnLight(position.x, position.y, size, 4.0f * 6.0f, 1, 0.5f, 0.4f, 1.0f, 0.05f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
                ParticleSpawner.spawnRocketShoot(position.x, position.y, size);
            }
        }
    }

    private void move(Direction direction, Vector2 rotationVector, float speed) {
        Vector2 force = rotationVector.product(speed);
        body.applyForce(force);
        onMove(direction);
    }

    public void move(Ship ship, Direction dir) {
        Engine engine = ship.engine;
        Vector2 rotationVector = new Vector2(body.getTransform().getRotationAngle());
        Vector2f pos = getPosition();

        if (dir == Direction.FORWARD) {
            move(Direction.FORWARD, rotationVector, engine.getForwardSpeed());
        } else if (dir == Direction.BACKWARD) {
            rotationVector.negate();
            move(Direction.BACKWARD, rotationVector, engine.getBackwardSpeed());
        } else if (dir == Direction.LEFT) {
            rotationVector.left();
            move(Direction.LEFT, rotationVector, engine.getSideSpeed());
        } else if (dir == Direction.RIGHT) {
            rotationVector.right();
            move(Direction.RIGHT, rotationVector, engine.getSideSpeed());
        } else if (dir == Direction.STOP) {
            body.getLinearVelocity().multiply(engine.getManeuverability() / 1.02f);

            float x = -(float) body.getLinearVelocity().x;
            float y = -(float) body.getLinearVelocity().y;

            if (Math.abs(x) > 10) {
                dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, x + pos.x, pos.y);
                onStopMove(dir);
            }

            if (Math.abs(y) > 10) {
                dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, pos.x, y + pos.y);
                onStopMove(dir);
            }
        }
    }

    private void onMove(Direction direction) {
        spawnEngineParticles(direction);
        if (this == world.getPlayerShip()) Core.get().sendUDPPacket(new PacketShipEngine(id, direction.ordinal()));
    }

    private void onStopMove(Direction direction) {
        spawnEngineParticles(direction);
        if (this == world.getPlayerShip()) {
            Core.get().sendUDPPacket(new PacketShipEngine(id, direction.ordinal()));
        }
    }

    public void collision(Body body, float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof Ship otherShip) {
                Vector2f velocityDif = new Vector2f((float) (body.getLinearVelocity().x - this.body.getLinearVelocity().x),
                        (float) (body.getLinearVelocity().y - this.body.getLinearVelocity().y));
                float impactPowerForOther = (float) ((velocityDif.length()) *
                        (this.body.getMass().getMass() / body.getMass().getMass()));

                impactPowerForOther /= 400.0f;

                if (impactPowerForOther > 0.25f) otherShip.damageByCollision(this, impactPowerForOther, contactX, contactY, normalX, normalY);
            } else if (userData instanceof Wreck) {
                if (collisionTimer <= 0) {
                    collisionTimer = 2;
                    onCollidedWithWreck(contactX, contactY, normalX, normalY);
                }
            }
        }
    }

    private void onCollidedWithWreck(float contactX, float contactY, float normalX, float normalY) {
        if (shield != null) {
            Vector4f color = shield.getColor();
            ParticleSpawner.spawnDirectedSpark(contactX, contactY, normalX, normalY, 4.5f, color.x, color.y, color.z, color.w);
        } else {
            ParticleSpawner.spawnDirectedSpark(contactX, contactY, normalX, normalY, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public void update() {
        super.update();

        if (spawned) {
            updateShip();
        } else {
            updateJump();
        }
    }

    protected void updateShip() {
        Vector2f position = getPosition();
        lastPosition.set(position.x, position.y);
        maskTexture.updateEffects();

        if (collisionTimer > 0) collisionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;

        if (this == world.getPlayerShip()) {
            Core.get().sendUDPPacket(new PacketObjectPosition(this));
            lifeTime = 0;
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
        } else {
            if (fixturesToAdd.size() > 0) {
                body.removeAllFixtures();
                for (int i = 0; i < fixturesToAdd.size(); i++) {
                    body.addFixture(fixturesToAdd.get(i));
                }
                fixturesToAdd.clear();
            }
        }
    }

    protected void updateJump() {
        if (((jumpVelocity.x < 0 && jumpPosition.x <= position.x) || (jumpVelocity.x >= 0 && jumpPosition.x >= position.x)) && ((jumpVelocity.y < 0 && jumpPosition.y <= position.y)
                || (jumpVelocity.y >= 0 && jumpPosition.y >= position.y))) {
            setSpawned();
            setVelocity(jumpVelocity.mul(0.26666668f));
            onShipSpawned();
        } else {
            lastJumpPosition.set(jumpPosition);
            jumpPosition.add(jumpVelocity.x * TimeUtils.UPDATE_DELTA_TIME, jumpVelocity.y * TimeUtils.UPDATE_DELTA_TIME);
            color.w += 1.5f * TimeUtils.UPDATE_DELTA_TIME;
        }
    }

    private void onShipSpawned() {
        Vector2f velocity = getVelocity();
        ParticleSpawner.spawnLight(position.x, position.y, velocity.x * 0.5f, velocity.y * 0.5f, 32.0f + scale.x * 0.25f, effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, 3.6f,
                true, RenderLayer.DEFAULT_ADDITIVE);
        ParticleSpawner.spawnDisableShield(position.x, position.y, velocity.x * 0.5f, velocity.y * 0.5f, 32.0f + scale.x * 0.25f, effectsColor.x, effectsColor.y, effectsColor.z, 1.0f);
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.jump, position.x, position.y));
    }

    @Override
    public void postPhysicsUpdate() {
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

    protected void shoot() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.tryShoot();
        }
    }

    protected void updateComponents() {
        if (shield != null) shield.update();
        if (armor != null) armor.update();
        if (reactor != null) reactor.update();
        if (hull != null) hull.regenHull(crew.getCrewRegen());

        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.update();
        }

        size = damages.size();
        for (int i = 0; i < size; i++) {
            Damage damage = damages.get(i);
            damage.update();
        }
    }

    private void damageByCollision(Ship otherShip, float impactPower, float contactX, float contactY, float normalX, float normalY) {
        if (collisionTimer > 0) {
            return;
        }

        lastAttacker = otherShip;
        if (otherShip.faction == faction) {
            impactPower /= 2.0f;
        }

        collisionTimer = 2;

        if (shield != null && shield.damage(impactPower)) {
            onShieldDamageByCollision(contactX, contactY, normalX, normalY);
            return;
        }

        float hullDamage = impactPower;
        float armorDamage = impactPower;
        Vector2f otherPos = otherShip.getPosition();
        Direction dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, otherPos.x, otherPos.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        onHullDamageByCollision(contactX, contactY, normalX, normalY);
    }

    private void onShieldDamageByCollision(float contactX, float contactY, float normalX, float normalY) {
        Vector4f color = shield.getColor();
        ParticleSpawner.spawnDirectedSpark(contactX, contactY, normalX, normalY, 4.5f, color.x, color.y, color.z, color.w);
    }

    protected void onHullDamageByCollision(float contactX, float contactY, float normalX, float normalY) {
        Random rand = world.getRand();
        ParticleSpawner.spawnDirectedSpark(contactX, contactY, normalX, normalY, 3.75f, 1.0f, 1.0f, 1.0f, 1.0f);
        Vector2f angletovel = RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.15f);
        ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), contactX, contactY, velocity.x * 0.25f + angletovel.x, velocity.y * 0.25f + angletovel.y, 2.0f * rand.nextFloat());
    }

    public boolean attackShip(BulletDamage damage, Ship attacker, Vector2f contactPoint, float multiplayer) {
        lastAttacker = attacker;
        float shieldDamage = damage.getBulletDamageShield() * multiplayer;

        if ((shield != null && shield.damage(shieldDamage))) {
            return false;
        }

        float hullDamage = damage.getBulletDamageHull() * multiplayer;
        float armorDamage = damage.getBulletDamageArmor() * multiplayer;
        Direction dir = CollisionObjectUtils.calculateDirectionToOtherObject(this, contactPoint.x, contactPoint.y);

        float reducedHullDamage = armor.reduceDamageByArmor(armorDamage, hullDamage, dir);
        hull.damage(reducedHullDamage);
        onHullDamage();
        return true;
    }

    public void onHullDamage() {}

    @Override
    public void setupFixture(BodyFixture bodyFixture) {
        bodyFixture.setFilter(new ShipFilter(this));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
    }

    protected void createName() {
        if (name != null) {
            stringObject.setString(name);
        }
    }

    private void createSpark() {
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

    protected abstract void createDestroyParticles();

    public void renderAdditive() {
        if (spawned) {
            renderGunSlotsAdditive();
            renderShield();
        } else {
            float size = 40.0f * color.w;
            SpriteRenderer.get().add(lastJumpPosition.x, lastJumpPosition.y, jumpPosition.x, jumpPosition.y, rotation, size, size,
                    effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, JUMP_TEXTURE, BufferType.ENTITIES_ADDITIVE);
        }
    }

    public void render() {
        if (spawned) {
            SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                    color.x, color.y, color.z, color.w, texture, maskTexture, BufferType.ENTITIES_ALPHA);

            if (hull.getHull() < hull.getMaxHull()) {
                float hp = hull.getHull() / hull.getMaxHull();
                SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin, cos, scale.x, scale.y,
                        1.0f, 1.0f, 1.0f, 1.0f - hp, textureDamage, maskTexture, BufferType.ENTITIES_ALPHA);
            }

            renderGunSlots();
            float yOffset = 3.2f + scale.y / 4.0f;
            stringObject.renderWithShadow(BufferType.ENTITIES_ALPHA, lastPosition.x, lastPosition.y + yOffset, position.x, position.y + yOffset, 0.1f, 0.1f, 0.1f, 0.1f);
        }
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
    }

    public WeaponSlot getWeaponSlot(int i) {
        return weaponSlots.get(i);
    }

    public void spawnEngineParticles(Direction dir) {

    }

    public void addDamage(Damage damage) {
        damages.add(damage);
    }

    public void setMoveDirection(Direction dir) {
        remoteMoveDirectionForEngineParticles = dir;
    }

    public void setSpawned() {
        color.w = 1.0f;
        spawned = true;
        world.spawnShip(this);
        createName();
    }

    public void setName(String name) {
        this.name = name;
        if (spawned) createName();
    }

    public void setVelocity(Vector2f velocity) {
        body.setLinearVelocity(new Vector2(velocity.x, velocity.y));
    }

    @Override
    public void updateClientPositionFromPacket(Vector2f position, float angle, Vector2f velocity, float angularVelocity) {
        super.updateClientPositionFromPacket(position, angle, velocity, angularVelocity);

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

    public abstract ShipType getType();

    public void setDestroying() {
        if (destroyingTimer == 0) destroyingTimer = maxDestroyingTimer;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void setDead() {
        super.setDead();
        createDestroyParticles();
        if (maskTexture != null) {
            maskTexture.delete();
        }
    }

    @Override
    public void clear() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            if (slot != null) slot.clear();
        }
    }
}