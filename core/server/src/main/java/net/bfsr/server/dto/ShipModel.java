package net.bfsr.server.dto;

import java.util.List;

public record ShipModel(
        String className,
        List<WeaponModel> weapons
) {}