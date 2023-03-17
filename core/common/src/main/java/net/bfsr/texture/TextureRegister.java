package net.bfsr.texture;

import lombok.Getter;

public enum TextureRegister {
    shipHumanSmall0("entity/ship/human_small0"),
    shipHumanSmall0Damage("entity/ship/human_small0_damage"),

    shipSaimonSmall0("entity/ship/saimon_small0"),
    shipSaimonSmall0Damage("entity/ship/saimon_small0_damage"),

    shipEngiSmall0("entity/ship/engi_small0"),
    shipEngiSmall0Damage("entity/ship/engi_small0_damage"),

    shipDamage0("particle/damage/damage0"),
    shipDamage1("particle/damage/damage1"),
    shipDamage2("particle/damage/damage2"),
    shipDamage3("particle/damage/damage3"),

    shipDamageFire0("particle/damage/fire0"),
    shipDamageFire1("particle/damage/fire1"),
    shipDamageFire2("particle/damage/fire2"),
    shipDamageFire3("particle/damage/fire3"),

    shipDamageLight2("particle/damage/light2"),
    shipDamageLight3("particle/damage/light3"),

    shipFix0("particle/damage/fix0"),
    shipFix1("particle/damage/fix1"),
    shipFix2("particle/damage/fix2"),
    shipFix3("particle/damage/fix3"),

    minerSmall("component/weapon/miner1"),

    plasmSmall("component/weapon/plasm1"),
    plasmMedium("component/weapon/plasm2"),
    plasmLarge("component/weapon/plasm3"),

    laserSmall("component/weapon/laser1"),
    laserMedium("component/weapon/laser2"),
    laserLarge("component/weapon/laser3"),

    gaussSmall("component/weapon/gauss1"),
    gaussMedium("component/weapon/gauss2"),
    gaussLarge("component/weapon/gauss3"),

    beamSmall("component/weapon/beam1"),
    beamMedium("component/weapon/beam2"),
    beamLarge("component/weapon/beam3"),

    canonSmall("component/weapon/canon1"),
    canonMedium("component/weapon/canon2"),
    canonLarge("component/weapon/canon3"),

    ionSmall("component/weapon/ion1"),
    ionMedium("component/weapon/ion2"),
    ionLarge("component/weapon/ion3"),

    shotgunSmall("component/weapon/shotgun1"),
    shotgunMedium("component/weapon/shotgun2"),
    shotgunLarge("component/weapon/shotgun3"),

    smallPlasm("entity/bullet/smallPlasm"),
    smallLaser("entity/bullet/smallLaser"),
    smallGaus("entity/bullet/smallGaus"),

    shieldSmall0("component/shield/small0"),

    guiBfsrText2("gui/bfsr_text2"),
    guiButtonBase("gui/buttonBase"),
    guiLogoBFSR("gui/logoBFSR"),
    guiSlider("gui/slider"),
    guiAdd("gui/add"),
    guiFactionSelect("gui/selectFaction"),
    guiArmorPlate("gui/armorPlate"),
    guiHudShip("gui/hudship"),
    guiHudShipAdd("gui/hudshipadd0"),
    guiMap("gui/map"),
    guiEnergy("gui/energy"),
    guiButtonControl("gui/buttonControl"),
    guiShield("gui/shield"),
    guiChat("gui/chat"),

    particleBeam("particle/beam"),
    particleBeamEffect("particle/beameffect"),
    particleBeamDamage("particle/beamdamage"),

    particleLight("particle/light"),

    particleShipEngineBack("particle/blue2"),

    particleSmoke0("particle/smoke0"),
    particleSmoke1("particle/smoke1"),
    particleSmoke2("particle/smoke2"),
    particleSmoke3("particle/smoke3"),
    particleSmoke4("particle/smoke4"),

    particleBlue3("particle/blue3"),

    particleJump("particle/jump"),
    particleDisableShield("particle/shielddown"),
    particleDirectedSpark("particle/directedspark"),
    particleDirectedSplat("particle/directedsplat"),

    particleExplosion("particle/explosion"),
    particleSmokeRing("particle/smokering"),

    particleGarbage0("particle/derbis0"),
    particleGarbage1("particle/derbis1"),
    particleGarbage2("particle/derbis2"),

    particleShipOst0("particle/shipost0"),
    particleShipOst1("particle/shipost1"),

    particleSpark0("particle/spark0"),
    particleSpark1("particle/spark1"),
    particleSpark2("particle/spark2"),
    particleSpark3("particle/spark3"),

    particleRocketEffect("particle/rocketeffect"),
    particleRocketSmoke("particle/rocketsmoke"),

    particleSockwaveSmall("particle/basicshockwave"),
    particleSockwaveMedium("particle/bigshockwave"),
    particleSockwaveLarge("particle/shockwave"),

    particleLighting("particle/lighting"),

    damageFire("effect/fire");

    @Getter
    private final String path;

    TextureRegister(String path) {
        this.path = path;
    }
}
