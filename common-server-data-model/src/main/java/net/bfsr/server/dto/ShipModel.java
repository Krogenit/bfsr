package net.bfsr.server.dto;

import java.util.List;

public record ShipModel(
        String name,
        List<WeaponModel> weapons
) {}