package net.bfsr.collision.filter;

import org.dyn4j.collision.Filter;

public class CollisionFilter implements Filter {

    protected Object userData;

    public CollisionFilter(Object userData) {
        this.userData = userData;
    }

    @Override
    public boolean isAllowed(Filter filter) {
        return false;
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }
}
