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
        this.creationDamage = damage;
        this.type = type;
        this.addPos = addPos;

        this.textureFire = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamageFire0.ordinal() + type]);
        this.textureFix = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipFix0.ordinal() + type]);
        this.textureDamage = texture;

        if (type > 1) {
            textureLight = TextureLoader.getTexture(TextureRegister.values()[TextureRegister.shipDamageLight2.ordinal() + type - 2]);
        }

        this.colorFire = new Vector4f(0.5f, 0.5f, 0.5f, 0.5f);
        this.colorLight = new Vector4f(0, 0, 0, 0);
        this.colorFix = new Vector4f(0, 0, 0, 0);
        this.rand = ship.getWorld().getRand();
        this.scale.x *= size;
        this.scale.y *= size;
    }

    public void updatePos() {
        this.rotate = ship.getRotation() + addRotation;
        Vector2f pos = ship.getPosition();
        float cos = ship.getCos();
        float sin = ship.getSin();
        float xPos = cos * addPos.x - sin * addPos.y;
        float yPos = sin * addPos.x + cos * addPos.y;
        this.position.x = pos.x + xPos;
        this.position.y = pos.y + yPos;
    }

    @Override
    public void update(double delta) {
        updatePos();

        Hull hull = ship.getHull();
        float hullValue = hull.getHull();
        float maxHull = hull.getMaxHull();
        damaged = hullValue / maxHull <= creationDamage;
//		damaged = true;
        if (damaged) {
            if (!isCreated) {
                addRotation = RotationHelper.TWOPI * rand.nextFloat();
                ParticleSpawner.spawnExplosion(position, scale.x, 20f);
                ParticleSpawner.spawnSpark(position, scale.x, 20f);
                ParticleSpawner.spawnLight(position, (scale.x + scale.y), new Vector4f(1.0f, 0.5f, 0.5f, 0.7f), 2f, true, EnumParticlePositionType.Default);
                isCreated = true;
            }

            double dt = 60f * delta;
            smokeTimer -= dt;
            ionTimer -= dt;
            if (smokeTimer <= 0) {
                ParticleSpawner.spawnDamageSmoke(position, 5F * rand.nextFloat() + scale.x / 4f, 10f, 0.4f);
                if (rand.nextInt(4) == 0) ParticleSpawner.spawnSmallGarbage(1, position.x, position.y, 0.01f, 10f);
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
            float fixSpeed = (float) (0.0005f * 60 * delta);
            if (colorFix.w >= 1.0f && repairTimer != 1000) {
                repairTimer = 1000;
                colorFix.w -= fixSpeed;
                colorFix.x -= fixSpeed;
                colorFix.y -= fixSpeed;
                colorFix.z -= fixSpeed;
            }
            repairTimer -= 60f * delta;
            if (repairTimer <= 0) {
                if (isCreated) {
                    ParticleSpawner.spawnLight(position, (scale.x + scale.y), new Vector4f(0.25f, 0.75f, 1f, 1f), 5f, true, EnumParticlePositionType.Default);
                    isCreated = false;
                }
                colorFix.w -= fixSpeed;
                colorFix.x -= fixSpeed;
                colorFix.y -= fixSpeed;
                colorFix.z -= fixSpeed;
            }
        }

        if (damaged || repairTimer > 0) {
            float fireSpeed = (float) (0.005f * 60 * delta);
            if (!changeFire) {
                if (colorFire.w < 1) {
                    colorFire.w += fireSpeed;
                    colorFire.x += fireSpeed;
                    colorFire.y += fireSpeed;
                    colorFire.z += fireSpeed;
                } else {
                    changeFire = true;
                }
            } else {
                if (colorFire.w > 0.5F) {
                    colorFire.w -= fireSpeed;
                    colorFire.x -= fireSpeed;
                    colorFire.y -= fireSpeed;
                    colorFire.z -= fireSpeed;
                } else {
                    changeFire = false;
                }
            }
            if (type > 1) {
                lightingTimer1 -= 60f * delta;
                float lightSpeed = (float) (0.2f * 60 * delta);
                if (lightingTimer1 <= 0) {
                    if (!changeLight) {
                        if (colorLight.w < 1F) {
                            colorLight.w += lightSpeed;
                            colorLight.x += lightSpeed;
                            colorLight.y += lightSpeed;
                            colorLight.z += lightSpeed;
                        } else {
                            changeLight = true;
                            lightingTimer += 25 + rand.nextInt(25);
                        }
                    } else {
                        if (colorLight.w > 0.0F) {
                            colorLight.w -= lightSpeed;
                            colorLight.x -= lightSpeed;
                            colorLight.y -= lightSpeed;
                            colorLight.z -= lightSpeed;
                        } else {
                            changeLight = false;
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
            this.texture = textureFix;
            this.color.w = colorFix.w;
            super.render(shader);
            this.texture = textureDamage;
            this.color.w = 1;
        }
    }

    public void renderEffects(BaseShader shader) {
        if (damaged || repairTimer > 0) {
            this.texture = textureFire;
            this.color.x = colorFire.x;
            this.color.y = colorFire.y;
            this.color.z = colorFire.z;
            this.color.w = colorFire.w;
            super.render(shader);
            if (textureLight != null) {
                this.texture = textureLight;
                this.color.x = colorLight.x;
                this.color.y = colorLight.y;
                this.color.z = colorLight.z;
                this.color.w = colorLight.w;
                super.render(shader);
            }

            this.texture = textureDamage;
            this.color.x = 1;
            this.color.y = 1;
            this.color.z = 1;
            this.color.w = 1;
        }
    }
}
