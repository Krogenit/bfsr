package net.bfsr.texture;

import lombok.Getter;

public enum TextureRegister {
    shipHumanSmall0("texture/entity/ship/human_small0.png"),
    shipHumanSmall0Damage("texture/entity/ship/human_small0_damage.png"),

    shipSaimonSmall0("texture/entity/ship/saimon_small0.png"),
    shipSaimonSmall0Damage("texture/entity/ship/saimon_small0_damage.png"),

    shipEngiSmall0("texture/entity/ship/engi_small0.png"),
    shipEngiSmall0Damage("texture/entity/ship/engi_small0_damage.png"),

    shipDamage0("texture/particle/damage/damage0.png"),
    shipDamage1("texture/particle/damage/damage1.png"),
    shipDamage2("texture/particle/damage/damage2.png"),
    shipDamage3("texture/particle/damage/damage3.png"),

    shipDamageFire0("texture/particle/damage/fire0.png"),
    shipDamageFire1("texture/particle/damage/fire1.png"),
    shipDamageFire2("texture/particle/damage/fire2.png"),
    shipDamageFire3("texture/particle/damage/fire3.png"),

    shipDamageLight2("texture/particle/damage/light2.png"),
    shipDamageLight3("texture/particle/damage/light3.png"),

    shipFix0("texture/particle/damage/fix0.png"),
    shipFix1("texture/particle/damage/fix1.png"),
    shipFix2("texture/particle/damage/fix2.png"),
    shipFix3("texture/particle/damage/fix3.png"),

    minerSmall("texture/component/weapon/miner1.png"),

    plasmSmall("texture/component/weapon/plasm1.png"),
    plasmMedium("texture/component/weapon/plasm2.png"),
    plasmLarge("texture/component/weapon/plasm3.png"),

    laserSmall("texture/component/weapon/laser1.png"),
    laserMedium("texture/component/weapon/laser2.png"),
    laserLarge("texture/component/weapon/laser3.png"),

    gaussSmall("texture/component/weapon/gauss1.png"),
    gaussMedium("texture/component/weapon/gauss2.png"),
    gaussLarge("texture/component/weapon/gauss3.png"),

    beamSmall("texture/component/weapon/beam1.png"),
    beamMedium("texture/component/weapon/beam2.png"),
    beamLarge("texture/component/weapon/beam3.png"),

    canonSmall("texture/component/weapon/canon1.png"),
    canonMedium("texture/component/weapon/canon2.png"),
    canonLarge("texture/component/weapon/canon3.png"),

    ionSmall("texture/component/weapon/ion1.png"),
    ionMedium("texture/component/weapon/ion2.png"),
    ionLarge("texture/component/weapon/ion3.png"),

    shotgunSmall("texture/component/weapon/shotgun1.png"),
    shotgunMedium("texture/component/weapon/shotgun2.png"),
    shotgunLarge("texture/component/weapon/shotgun3.png"),

    smallPlasm("texture/entity/bullet/smallPlasm.png"),
    smallLaser("texture/entity/bullet/smallLaser.png"),
    smallGaus("texture/entity/bullet/smallGaus.png"),

    shieldSmall0("texture/component/shield/small0.png"),

    guiBfsrText2("texture/gui/bfsr_text2.png"),
    guiButtonBase("texture/gui/buttonBase.png"),
    guiLogoBFSR("texture/gui/logoBFSR.png"),
    guiSlider("texture/gui/slider.png"),
    guiAdd("texture/gui/add.png"),
    guiFactionSelect("texture/gui/selectFaction.png"),
    guiArmorPlate("texture/gui/armorPlate.png"),
    guiHudShip("texture/gui/hudship.png"),
    guiHudShipAdd("texture/gui/hudshipadd0.png"),
    guiMap("texture/gui/map.png"),
    guiEnergy("texture/gui/energy.png"),
    guiButtonControl("texture/gui/buttonControl.png"),
    guiShield("texture/gui/shield.png"),
    guiChat("texture/gui/chat.png"),

    particleBeam("texture/particle/beam.png"),
    particleBeamEffect("texture/particle/beameffect.png"),
    particleBeamDamage("texture/particle/beamdamage.png"),

    particleLight("texture/particle/light.png"),

    particleShipEngineBack("texture/particle/blue2.png"),

    particleSmoke0("texture/particle/smoke0.png"),
    particleSmoke1("texture/particle/smoke1.png"),
    particleSmoke2("texture/particle/smoke2.png"),
    particleSmoke3("texture/particle/smoke3.png"),
    particleSmoke4("texture/particle/smoke4.png"),

    particleBlue3("texture/particle/blue3.png"),

    particleJump("texture/particle/jump.png"),
    particleDisableShield("texture/particle/shielddown.png"),
    particleDirectedSpark("texture/particle/directedspark.png"),
    particleDirectedSplat("texture/particle/directedsplat.png"),

    particleExplosion("texture/particle/explosion.png"),
    particleSmokeRing("texture/particle/smokering.png"),

    particleGarbage0("texture/particle/derbis0.png"),
    particleGarbage1("texture/particle/derbis1.png"),
    particleGarbage2("texture/particle/derbis2.png"),

    particleShipOst0("texture/particle/shipost0.png"),
    particleShipOst1("texture/particle/shipost1.png"),

    particleSpark0("texture/particle/spark0.png"),
    particleSpark1("texture/particle/spark1.png"),
    particleSpark2("texture/particle/spark2.png"),
    particleSpark3("texture/particle/spark3.png"),

    particleRocketEffect("texture/particle/rocketeffect.png"),
    particleRocketSmoke("texture/particle/rocketsmoke.png"),

    particleSockwaveSmall("texture/particle/basicshockwave.png"),
    particleSockwaveMedium("texture/particle/bigshockwave.png"),
    particleSockwaveLarge("texture/particle/shockwave.png"),

    particleLighting("texture/particle/lighting.png"),

    damageFire("texture/effect/fire.png");

    @Getter
    private final String path;

    TextureRegister(String path) {
        this.path = path;
    }
}