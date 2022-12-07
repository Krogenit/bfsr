package net.bfsr.component.weapon.small;

import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WeaponBeamSmall extends WeaponSlotBeam {
    public WeaponBeamSmall(Ship ship) {
        super(ship, 400.0f, new BulletDamage(0.075f, 0.075f, 0.15f), new Vector4f(0.8f, 0.8f, 1.0f, 1.0f), 125.0f, 6.0f, new Vector2f(14, 10), TextureRegister.beamSmall, new SoundRegistry[]{SoundRegistry.weaponShootBeam0, SoundRegistry.weaponShootBeam1, SoundRegistry.weaponShootBeam2});
    }

    @Override
    public void createBody() {
        Vector2f scale = new Vector2f(24, 16);
        float offset = 5.0f;
        float width = scale.x - offset;
        float height = scale.y - offset;
        float addX = addPosition.x;
        float addY = addPosition.y;
        Vector2[] vertecies = {
                new Vector2(-width * 0.5 + addX, -height * 0.5 + addY),
                new Vector2(width * 0.5 + addX, -height * 0.5 + addY),
                new Vector2(width * 0.5 + addX, height * 0.5 + addY),
                new Vector2(-width * 0.5 + addX, height * 0.5 + addY)
        };
        Polygon rectangle = Geometry.createPolygon(vertecies);
        BodyFixture bodyFixture = new BodyFixture(rectangle);
        bodyFixture.setUserData(this);
        bodyFixture.setFilter(new ShipFilter(ship));
        ship.getBody().addFixture(bodyFixture);
        ship.recalculateMass();
    }
}
