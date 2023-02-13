package net.bfsr.component.damage;

import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.component.hull.Hull;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.MathUtils;
import net.bfsr.util.TimeUtils;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class Damage extends TextureObject {
    private final Ship ship;
    private final int type;
    private boolean holeCreated, fireFadingOut, lightFadingOut, damaged;
    private float addRotation;
    private float ionTimer;
    private final float creationDamage;
    private float repairTimer;
    private float smokeTimer;
    private float sparkleFlickerTimer;
    private float sparkleActivationTimer;
    private Texture textureLight;
    private final Texture textureFire;
    private final Texture textureFix;
    private final Vector2f addPos;
    private final Vector4f colorFix = new Vector4f(1.0f, 1.0f, 1.0f, 0.0f);
    private final Vector4f colorFire = new Vector4f(1.0f, 1.0f, 1.0f, 0.5f);
    private final Vector4f colorLight = new Vector4f(1.0f, 1.0f, 1.0f, 0.0f);
    private final Random rand;

    public Damage(Ship ship, float damage, int type, Vector2f addPos, float size) {
        super(TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamage0.ordinal() + type]), ship.getPosition().x, ship.getPosition().y);
        this.ship = ship;
        this.creationDamage = damage;
        this.type = type;
        this.addPos = addPos;

        this.textureFire = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamageFire0.ordinal() + type]);
        this.textureFix = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipFix0.ordinal() + type]);

        if (type > 1) {
            this.textureLight = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamageLight2.ordinal() + type - 2]);
        }

        this.rand = ship.getWorld().getRand();
        this.scale.x *= size;
        this.scale.y *= size;
    }

    public void updatePos() {
        rotation = ship.getRotation() + addRotation;
        Vector2f pos = ship.getPosition();
        float cos = ship.getCos();
        float sin = ship.getSin();
        float xPos = cos * addPos.x - sin * addPos.y;
        float yPos = sin * addPos.x + cos * addPos.y;
        position.x = pos.x + xPos;
        position.y = pos.y + yPos;
    }

    @Override
    public void update() {
        lastRotation = rotation;
        lastPosition.set(position);

        updatePos();

        Hull hull = ship.getHull();
        float hullValue = hull.getHull();
        float maxHull = hull.getMaxHull();
        damaged = hullValue / maxHull <= creationDamage;

        if (damaged) {
            if (!holeCreated) {
                addRotation = MathUtils.TWO_PI * rand.nextFloat();
                ParticleSpawner.spawnExplosion(position.x, position.y, scale.x, 2.0f);
                ParticleSpawner.spawnSpark(position.x, position.y, scale.x, 2.0f);
                ParticleSpawner.spawnLight(position.x, position.y, (scale.x + scale.y), 1.0f, 0.5f, 0.5f, 0.7f, 2.0f, true, RenderLayer.DEFAULT_ADDITIVE);
                holeCreated = true;
            }

            smokeTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            ionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (smokeTimer <= 0) {
                ParticleSpawner.spawnDamageSmoke(position.x, position.y, 0.5F * rand.nextFloat() + scale.x / 4.0f, 1.0f, 0.4f);
                if (rand.nextInt(4) == 0) ParticleSpawner.spawnSmallGarbage(1, position.x, position.y, 0.01f, 1.0f);
                smokeTimer = 1 + rand.nextInt(2);//3 + rand.nextInt(10);
            }
            if (ionTimer <= 0 && type > 1) {
                ParticleSpawner.spawnLightingIon(position, (scale.x + scale.y) / 1.5f);
                ionTimer = 200 + rand.nextInt(120);
            }
            colorFix.w = 1.0f;
        } else if (colorFix.w > 0) {
            float fixSpeed = 0.03f * TimeUtils.UPDATE_DELTA_TIME;
            if (colorFix.w >= 1.0f && repairTimer != 1000) {
                repairTimer = 1000;
                colorFix.w -= fixSpeed;
                if (colorFix.w < 0.0f) colorFix.w = 0.0f;
            }
            repairTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (repairTimer <= 0) {
                if (holeCreated) {
                    ParticleSpawner.spawnLight(position.x, position.y, (scale.x + scale.y), 0.25f, 0.75f, 1.0f, 1.0f, 5.0f, true, RenderLayer.DEFAULT_ADDITIVE);
                    holeCreated = false;
                }
                colorFix.z -= fixSpeed;
                if (colorFix.w < 0.0f) colorFix.w = 0.0f;
            }
        }

        if (damaged || repairTimer > 0) {
            float fireSpeed = 0.3f * TimeUtils.UPDATE_DELTA_TIME;
            if (fireFadingOut) {
                if (colorFire.w > 0.5f) {
                    colorFire.w -= fireSpeed;
                    if (colorFire.w < 0.0f) colorFire.w = 0.0f;
                } else {
                    fireFadingOut = false;
                }
            } else {
                if (colorFire.w < 1.0f) {
                    colorFire.w += fireSpeed;
                } else {
                    fireFadingOut = true;
                }
            }
            if (type > 1) {
                sparkleActivationTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;
                if (sparkleActivationTimer <= 0) {
                    if (lightFadingOut) {
                        if (colorLight.w > 0.0f) {
                            colorLight.w -= lightSpeed;
                            if (colorLight.w < 0.0f) colorLight.w = 0.0f;
                        } else {
                            lightFadingOut = false;
                            sparkleFlickerTimer += 25 + rand.nextInt(25);
                        }
                    } else {
                        if (colorLight.w < 1.0F) {
                            colorLight.w += lightSpeed;
                        } else {
                            lightFadingOut = true;
                            sparkleFlickerTimer += 25 + rand.nextInt(25);
                        }
                    }
                } else if (colorLight.w > 0.0f) {
                    colorLight.w -= lightSpeed;
                    if (colorLight.w < 0.0f) colorLight.w = 0.0f;
                }

                if (sparkleFlickerTimer >= 100) {
                    sparkleActivationTimer = 60 + rand.nextInt(240);
                    sparkleFlickerTimer = 0;
                }
            }
        }
    }

    public void render() {
        if (damaged || repairTimer > 0) {
            SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                    scale.x, scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
        } else if (repairTimer <= 0 && colorFix.w > 0) {
            SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                    scale.x, scale.y, color.x, color.y, color.z, colorFix.w, textureFix, BufferType.ENTITIES_ALPHA);
        }
    }

    public void renderEffects() {
        if (damaged || repairTimer > 0) {
            if (colorFire.w > 0) {
                SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                        scale.x, scale.y, colorFire.x, colorFire.y, colorFire.z, colorFire.w, textureFire, BufferType.ENTITIES_ADDITIVE);
            }
            if (textureLight != null && colorLight.w > 0) {
                SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                        scale.x, scale.y, colorLight.x, colorLight.y, colorLight.z, colorLight.w, textureLight, BufferType.ENTITIES_ADDITIVE);
            }
        }
    }
}
