package net.bfsr.client.sound;

public enum SoundRegistry {
    buttonCollide("sound/gui/buttonCollide.ogg", 0.275f),
    buttonClick("sound/gui/buttonClick.ogg", 2.0f),

    weaponShootPlasm0("sound/weapon/plasm0.ogg", 1.0f),
    weaponShootPlasm1("sound/weapon/plasm1.ogg", 1.0f),
    weaponShootPlasm2("sound/weapon/plasm2.ogg", 1.0f),
    weaponShootLaser0("sound/weapon/laser0.ogg", 1.0f),
    weaponShootLaser1("sound/weapon/laser1.ogg", 1.0f),
    weaponShootGaus0("sound/weapon/gaus0.ogg", 1.0f),
    weaponShootGaus1("sound/weapon/gaus1.ogg", 1.0f),
    weaponShootGaus2("sound/weapon/gaus2.ogg", 1.0f),

    weaponShootBeam0("sound/weapon/beam0.ogg", 1.0f),
    weaponShootBeam1("sound/weapon/beam1.ogg", 1.0f),
    weaponShootBeam2("sound/weapon/beam2.ogg", 1.0f),
    weaponShootBeamLarge("sound/weapon/beamlarge.ogg", 1.0f),

    explosion0("sound/explosion/explosion0.ogg", 1.0f),
    explosion1("sound/explosion/explosion1.ogg", 1.0f),
    explosion2("sound/explosion/explosion2.ogg", 1.0f),

    damage("sound/explosion/damage.ogg", 1.0f),
    damageNoShield("sound/explosion/damagenoshield.ogg", 1.0f),

    jump("sound/jump/jump.ogg", 1.0f),

    shieldDown("sound/shield/shielddown.ogg", 1.0f),
    shieldUp0("sound/shield/shieldup0.ogg", 1.0f),
    shieldUp1("sound/shield/shieldup1.ogg", 1.0f);

    private final String path;
    private final float volume;

    SoundRegistry(String path, float volume) {
        this.path = path;
        this.volume = volume;
    }

    public String getPath() {
        return path;
    }

    public float getVolume() {
        return volume;
    }
}