package net.bfsr.physics.filter;

import org.jbox2d.dynamics.Filter;

public class BeamFilter extends Filter {
    BeamFilter() {
        super(Categories.BEAM_CATEGORY, Categories.SHIP_CATEGORY);
    }
}