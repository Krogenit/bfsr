package net.bfsr.component.shield;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;

import java.util.List;

public class ShieldCommon {
    @Getter
    @Setter
    protected float shield;
    @Getter
    protected final float maxShield;
    private final float shieldRegen;
    private final Vector2f radius = new Vector2f();
    protected final Vector2f diameter = new Vector2f();
    protected final float timeToRebuild;
    protected float rebuildingTime;
    @Getter
    protected float size;
    protected final Body body;
    protected boolean alive;
    private BodyFixture shieldFixture;

    protected ShieldCommon(Body body, float maxShield, float shieldRegen, float timeToRebuild) {
        this.body = body;
        this.size = 1.0f;
        this.maxShield = maxShield;
        this.shieldRegen = shieldRegen;
        this.timeToRebuild = timeToRebuild;
        this.rebuildingTime = timeToRebuild;
        this.shield = maxShield;
    }

    public void createBody() {
        List<BodyFixture> fixtures = body.getFixtures();
        if (shieldFixture != null) {
            body.removeFixture(shieldFixture);
            shieldFixture = null;
        }

        for (int i = 0; i < fixtures.size(); i++) {
            BodyFixture bodyFixture = fixtures.get(i);
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Polygon polygon) {
                for (Vector2 vertex : polygon.getVertices()) {
                    float x1 = (float) Math.abs(vertex.x);
                    if (x1 > radius.x) {
                        radius.x = x1;
                    }
                    float y1 = (float) Math.abs(vertex.y);
                    if (y1 > radius.y) {
                        radius.y = y1;
                    }
                }
            }
        }

        float offset = 1.4f;
        diameter.set(radius.x * 2.0f + offset, radius.y * 2.0f + offset);

        Ellipse ellipse = Geometry.createEllipse(diameter.x, diameter.y);
        shieldFixture = new BodyFixture(ellipse);
        shieldFixture.setUserData(this);
        shieldFixture.setDensity(PhysicsUtils.SHIELD_FIXTURE_DENSITY);
        shieldFixture.setFriction(0.0f);
        shieldFixture.setRestitution(0.1f);
        shieldFixture.setFilter(body.getFixture(0).getFilter());
        body.addFixture(shieldFixture);
        body.setMass(MassType.NORMAL);
        diameter.x += 0.1f;
        diameter.y += 0.1f;
        alive = true;
    }

    public void update() {
        if (shield < maxShield && isShieldAlive()) {
            shield += shieldRegen * TimeUtils.UPDATE_DELTA_TIME;

            onShieldAlive();

            if (shield > maxShield) {
                shield = maxShield;
            }
        }
    }

    protected void onShieldAlive() {}

    public boolean isShieldAlive() {
        return rebuildingTime >= timeToRebuild;
    }

    public void rebuildShield() {
        shield = maxShield / 5.0f;
        rebuildingTime = timeToRebuild;
        createBody();
    }

    public boolean damage(float shieldDamage) {
        if (shield > 0) {
            shield -= shieldDamage;
            return true;
        }

        onNoShieldDamage();
        return false;
    }

    protected void onNoShieldDamage() {}

    public void setRebuildingTime(int time) {
        rebuildingTime = time;
    }

    public void removeShield() {
        body.removeFixture(shieldFixture);
        shieldFixture = null;
        body.setMass(MassType.NORMAL);
        rebuildingTime = 0;
        size = 0.0f;
        shield = 0;
        alive = false;
    }
}
