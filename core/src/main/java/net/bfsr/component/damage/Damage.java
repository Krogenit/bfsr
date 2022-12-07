package net.bfsr.component.damage;

import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.Texture;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.component.hull.Hull;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import net.bfsr.util.TimeUtils;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class Damage extends TextureObject {
    private final Ship ship;
    private final int type;
    private boolean isCreated, changeFire, changeLight, damaged;
    private float addRotation;
    private float ionTimer;
    private final float creationDamage;
    private float repairTimer;
    private float smokeTimer;
    private float lightingTimer;
    private float lightingTimer1;
    private final Texture textureDamage;
    private Texture textureLight;
    private final Texture textureFire;
    private final Texture textureFix;
    private final Vector2f addPos;
    private final Vector4f colorFix;
    private final Vector4f colorFire;
    private final Vector4f colorLight;
    private final Random rand;

    public Damage(Ship ship, float damage, int type, Vector2f addPos, float size) {
        super(TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamage0.ordinal() + type]), new Vector2f(ship.getPosition()));
        this.ship = ship;
        creationDamage = damage;
        this.type = type;
        this.addPos = addPos;

        textureFire = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamageFire0.ordinal() + type]);
        textureFix = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipFix0.ordinal() + type]);
        textureDamage = texture;

        if (type > 1) {
            textureLight = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamageLight2.ordinal() + type - 2]);
        }

        colorFire = new Vector4f(0.5f, 0.5f, 0.5f, 0.5f);
        colorLight = new Vector4f(0, 0, 0, 0);
        colorFix = new Vector4f(0, 0, 0, 0);
        rand = ship.getWorld().getRand();
        scale.x *= size;
        scale.y *= size;
    }

    public void updatePos() {
        rotate = ship.getRotation() + addRotation;
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
        updatePos();

        Hull hull = ship.getHull();
        float hullValue = hull.getHull();
        float maxHull = hull.getMaxHull();
        damaged = hullValue / maxHull <= creationDamage;
//		damaged = true;
        if (damaged) {
            if (!isCreated) {
                addRotation = RotationHelper.TWOPI * rand.nextFloat();
                ParticleSpawner.spawnExplosion(position, scale.x, 2.0f);
                ParticleSpawner.spawnSpark(position, scale.x, 2.0f);
                ParticleSpawner.spawnLight(position, (scale.x + scale.y), new Vector4f(1.0f, 0.5f, 0.5f, 0.7f), 2.0f, true, EnumParticlePositionType.Default);
                isCreated = true;
            }

            smokeTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            ionTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (smokeTimer <= 0) {
                ParticleSpawner.spawnDamageSmoke(position, 0.5F * rand.nextFloat() + scale.x / 4.0f, 1.0f, 0.4f);
                if (rand.nextInt(4) == 0) ParticleSpawner.spawnSmallGarbage(1, position.x, position.y, 0.01f, 1.0f);
                smokeTimer = 1 + rand.nextInt(2);//3 + rand.nextInt(10);
            }
            if (ionTimer <= 0 && type > 1) {
                ParticleSpawner.spawnLightingIon(position, (scale.x + scale.y) / 1.5f);
                ionTimer = 200 + rand.nextInt(120);
            }
            colorFix.x = 1;
            colorFix.y = 1;
            colorFix.z = 1;
            colorFix.w = 1;
        } else if (colorFix.w > 0) {
            float fixSpeed = 0.03f * TimeUtils.UPDATE_DELTA_TIME;
            if (colorFix.w >= 1.0f && repairTimer != 1000) {
                repairTimer = 1000;
                colorFix.w -= fixSpeed;
                colorFix.x -= fixSpeed;
                colorFix.y -= fixSpeed;
                colorFix.z -= fixSpeed;
            }
            repairTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (repairTimer <= 0) {
                if (isCreated) {
                    ParticleSpawner.spawnLight(position, (scale.x + scale.y), new Vector4f(0.25f, 0.75f, 1.0f, 1.0f), 5.0f, true, EnumParticlePositionType.Default);
                    isCreated = false;
                }
                colorFix.w -= fixSpeed;
                colorFix.x -= fixSpeed;
                colorFix.y -= fixSpeed;
                colorFix.z -= fixSpeed;
            }
        }

        if (damaged || repairTimer > 0) {
            float fireSpeed = 0.3f * TimeUtils.UPDATE_DELTA_TIME;
            if (changeFire) {
                if (colorFire.w > 0.5F) {
                    colorFire.w -= fireSpeed;
                    colorFire.x -= fireSpeed;
                    colorFire.y -= fireSpeed;
                    colorFire.z -= fireSpeed;
                } else {
                    changeFire = false;
                }
            } else {
                if (colorFire.w < 1) {
                    colorFire.w += fireSpeed;
                    colorFire.x += fireSpeed;
                    colorFire.y += fireSpeed;
                    colorFire.z += fireSpeed;
                } else {
                    changeFire = true;
                }
            }
            if (type > 1) {
                lightingTimer1 -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;
                if (lightingTimer1 <= 0) {
                    if (changeLight) {
                        if (colorLight.w > 0.0F) {
                            colorLight.w -= lightSpeed;
                            colorLight.x -= lightSpeed;
                            colorLight.y -= lightSpeed;
                            colorLight.z -= lightSpeed;
                        } else {
                            changeLight = false;
                            lightingTimer += 25 + rand.nextInt(25);
                        }
                    } else {
                        if (colorLight.w < 1.0F) {
                            colorLight.w += lightSpeed;
                            colorLight.x += lightSpeed;
                            colorLight.y += lightSpeed;
                            colorLight.z += lightSpeed;
                        } else {
                            changeLight = true;
                            lightingTimer += 25 + rand.nextInt(25);
                        }
                    }
                } else if (colorLight.w > 0.0F) {
                    colorLight.w -= lightSpeed;
                    colorLight.x -= lightSpeed;
                    colorLight.y -= lightSpeed;
                    colorLight.z -= lightSpeed;
                }

                if (lightingTimer >= 100) {
                    lightingTimer1 = 60 + rand.nextInt(240);
                    lightingTimer = 0;

                }
            }

        }
    }

    @Override
    public void render(BaseShader shader) {
        if (damaged || repairTimer > 0) {
            super.render(shader);
        } else if (repairTimer <= 0) {
            texture = textureFix;
            color.w = colorFix.w;
            super.render(shader);
            texture = textureDamage;
            color.w = 1;
        }
    }

    public void renderEffects(BaseShader shader) {
        if (damaged || repairTimer > 0) {
            texture = textureFire;
            color.x = colorFire.x;
            color.y = colorFire.y;
            color.z = colorFire.z;
            color.w = colorFire.w;
            super.render(shader);
            if (textureLight != null) {
                texture = textureLight;
                color.x = colorLight.x;
                color.y = colorLight.y;
                color.z = colorLight.z;
                color.w = colorLight.w;
                super.render(shader);
            }

            texture = textureDamage;
            color.x = 1;
            color.y = 1;
            color.z = 1;
            color.w = 1;
        }
    }
}