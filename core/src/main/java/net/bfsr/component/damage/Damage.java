package net.bfsr.component.damage;

import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.component.hull.Hull;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.ModelMatrixUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.util.TimeUtils;
import org.joml.Matrix4f;
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
    private Texture textureLight;
    private final Texture textureFire;
    private final Texture textureFix;
    private final Vector2f addPos;
    private final Vector4f colorFix;
    private final Vector4f colorFire;
    private final Vector4f colorLight;
    private final Random rand;

    public Damage(Ship ship, float damage, int type, Vector2f addPos, float size) {
        super(TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamage0.ordinal() + type]), ship.getPosition().x, ship.getPosition().y);
        this.ship = ship;
        creationDamage = damage;
        this.type = type;
        this.addPos = addPos;

        textureFire = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamageFire0.ordinal() + type]);
        textureFix = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipFix0.ordinal() + type]);

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
        rotation = ship.getRotation() + addRotation;
        Vector2f pos = ship.getPosition();
        float cos = ship.getCos();
        float sin = ship.getSin();
        float xPos = cos * addPos.x - sin * addPos.y;
        float yPos = sin * addPos.x + cos * addPos.y;
        lastPosition.set(position);
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
                ParticleSpawner.spawnExplosion(position.x, position.y, scale.x, 2.0f);
                ParticleSpawner.spawnSpark(position.x, position.y, scale.x, 2.0f);
                ParticleSpawner.spawnLight(position.x, position.y, (scale.x + scale.y), 1.0f, 0.5f, 0.5f, 0.7f, 2.0f, true, RenderLayer.DEFAULT_ADDITIVE);
                isCreated = true;
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
                    ParticleSpawner.spawnLight(position.x, position.y, (scale.x + scale.y), 0.25f, 0.75f, 1.0f, 1.0f, 5.0f, true, RenderLayer.DEFAULT_ADDITIVE);
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
    public void render(BaseShader shader, float interpolation) {
        if (damaged || repairTimer > 0) {
            InstancedRenderer.INSTANCE.addToRenderPipeLine(ModelMatrixUtils.getModelMatrix(this, interpolation), color.x, color.y, color.z, color.w, texture);
        } else if (repairTimer <= 0) {
            InstancedRenderer.INSTANCE.addToRenderPipeLine(ModelMatrixUtils.getModelMatrix(this, interpolation), color.x, color.y, color.z, colorFix.w, textureFix);
        }
    }

    public void renderEffects(float interpolation) {
        if (damaged || repairTimer > 0) {
            Matrix4f modelMatrix = ModelMatrixUtils.getModelMatrix(this, interpolation);
            InstancedRenderer.INSTANCE.addToRenderPipeLine(modelMatrix, colorFire.x, colorFire.y, colorFire.z, colorFire.w, textureFire);
            if (textureLight != null) {
                InstancedRenderer.INSTANCE.addToRenderPipeLine(modelMatrix, colorLight.x, colorLight.y, colorLight.z, colorLight.w, textureLight);
            }
        }
    }
}
