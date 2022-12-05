package net.bfsr.component;

import net.bfsr.client.texture.TextureRegister;

public enum EnumWeapon {

    PlasmSmall("plasm1", TextureRegister.plasmSmall);

    private final String name;
    private final TextureRegister texture;

    EnumWeapon(String name, TextureRegister texture) {
        this.name = name;
        this.texture = texture;
    }

    public EnumWeapon getByName(String name) {
        EnumWeapon[] enums = values();
        for (EnumWeapon enumWeapon : enums) {
            if (enumWeapon.toString().equals(name)) return enumWeapon;
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public TextureRegister getTexture() {
        return texture;
    }
}
