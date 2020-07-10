package ru.krogenit.bfsr.collision.filter;

import org.dyn4j.collision.Filter;

public class ShieldFilter implements Filter {

	@Override
	public boolean isAllowed(Filter filter) {
		return false;
	}

}
