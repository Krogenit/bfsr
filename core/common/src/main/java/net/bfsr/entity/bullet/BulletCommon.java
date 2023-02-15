package net.bfsr.entity.bullet;

import lombok.Getter;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.entity.wreck.WreckCommon;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public abstract class BulletCommon extends CollisionObject {
    @Getter
    protected final ShipCommon ship;
    private final float bulletSpeed;
    private final float alphaReducer;
    @Getter
    private final BulletDamage damage;
    private float energy;
    private Object previousAObject;

    protected BulletCommon(World<?> world, int id, float bulletSpeed, float x, float y, float sin, float cos, float scaleX, float scaleY, ShipCommon ship,
                           float r, float g, float b, float a, float alphaReducer, BulletDamage damage) {
        super(world, id, x, y, sin, cos, scaleX, scaleY, r, g, b, a);
        this.alphaReducer = alphaReducer;
        this.damage = damage;
        this.ship = ship;
        this.bulletSpeed = bulletSpeed;
        energy = damage.getAverageDamage();
        setBulletVelocityAndStartTransform(x, y);
        world.addBullet(this);
    }

    private void setBulletVelocityAndStartTransform(float x, float y) {
        velocity.set(cos * bulletSpeed, sin * bulletSpeed);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(sin, cos);
        body.getTransform().setTranslation(x + velocity.x / 500.0f, y + velocity.y / 500.0f);//TODO: посчитать точку появления пули правильно
    }

    @Override
    public void update() {
        super.update();
        lastPosition.set(getPosition());

        color.w -= alphaReducer * TimeUtils.UPDATE_DELTA_TIME;

        if (color.w <= 0) {
            setDead(true);
        }
    }

    @Override
    public void postPhysicsUpdate() {
        Vector2 velocity = body.getLinearVelocity();
        float rotateToVector = (float) Math.atan2(-velocity.x, velocity.y) + MathUtils.HALF_PI;
        sin = LUT.sin(rotateToVector);
        cos = LUT.cos(rotateToVector);
        lastSin = sin;
        lastCos = cos;
        body.getTransform().setRotation(sin, cos);
        updateWorldAABB();
    }

    public void checkCollision(Contact contact, Vector2 normal, Body body) {
        Object userData = body.getUserData();
        if (userData != null) {
            if (userData instanceof ShipCommon ship) {
                if (canDamageShip(ship)) {
                    previousAObject = ship;
                    Vector2f position = getPosition();
                    if (damageShip(ship)) {
                        //Hull damage
                        destroyBullet(ship, contact, normal);
                        setDead(true);
                        onDamageShipWithNoShield();
                    } else {
                        //Shield reflection
                        destroyBullet(ship, contact, normal);
                        damage(this);
                        onDamageShipWithShield();
                    }
                } else if (previousAObject != null && previousAObject != ship && this.ship == ship) {
                    previousAObject = ship;
                    //We can damage ship after some collission with other object
                    destroyBullet(ship, contact, normal);
                }
            } else if (userData instanceof BulletCommon bullet) {
                //Bullet vs bullet
                bullet.damage(this);
                previousAObject = bullet;

                if (bullet.isDead()) {
                    bullet.destroyBullet(this, contact, normal);
                }
            } else if (userData instanceof WreckCommon wreck) {
                wreck.damage(damage.bulletDamageHull);
                destroyBullet(wreck, contact, normal);
            }
        }
    }

    protected void onDamageShipWithNoShield() {

    }

    protected void onDamageShipWithShield() {

    }

    private void damage(BulletCommon bullet) {
        float damage = bullet.damage.getAverageDamage();
        damage /= 3.0f;

        this.damage.bulletDamageArmor -= damage;
        this.damage.bulletDamageHull -= damage;
        this.damage.bulletDamageShield -= damage;

        if (this.damage.bulletDamageArmor < 0) setDead(true);
        else if (this.damage.bulletDamageHull < 0) setDead(true);
        else if (this.damage.bulletDamageShield < 0) setDead(true);

        if (bullet != this) {
            energy -= damage;

            if (energy <= 0) {
                setDead(true);
            }
        }
    }

    protected abstract void destroyBullet(CollisionObject destroyer, Contact contact, Vector2 normal);

    public boolean canDamageShip(ShipCommon ship) {
        return this.ship != ship && previousAObject != ship;
    }

    private boolean damageShip(ShipCommon ship) {
        return ship.attackShip(damage, ship, getPosition(), ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f);
    }
}
