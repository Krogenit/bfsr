package net.bfsr.client.sound;

public enum SoundRegistry {
    buttonCollide("gui/buttonCollide", 0.275f),
    buttonClick("gui/buttonClick", 2.0f),

    weaponShootPlasm0("weapon/plasm0", 1.0f),
    weaponShootPlasm1("weapon/plasm1", 1.0f),
    weaponShootPlasm2("weapon/plasm2", 1.0f),
    weaponShootLaser0("weapon/laser0", 1.0f),
    weaponShootLaser1("weapon/laser1", 1.0f),
    weaponShootGaus0("weapon/gaus0", 1.0f),
    weaponShootGaus1("weapon/gaus1", 1.0f),
    weaponShootGaus2("weapon/gaus2", 1.0f),

    weaponShootBeam0("weapon/beam0", 1.0f),
    weaponShootBeam1("weapon/beam1", 1.0f),
    weaponShootBeam2("weapon/beam2", 1.0f),
    weaponShootBeamLarge("weapon/beamlarge", 1.0f),

    explosion0("explosion/explosion0", 1.0f),
    explosion1("explosion/explosion1", 1.0f),
    explosion2("explosion/explosion2", 1.0f),

    damage("explosion/damage", 1.0f),
    damageNoShield("explosion/damagenoshield", 1.0f),

    jump("jump/jump", 1.0f),

    shieldDown("shield/shielddown", 1.0f),
    shieldUp0("shield/shieldup0", 1.0f),
    shieldUp1("shield/shieldup1", 1.0f);

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
