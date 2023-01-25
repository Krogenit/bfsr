package net.bfsr.collision.filter;

import org.dyn4j.collision.Filter;

public class CollisionFilter<T> implements Filter {
    protected T userData;

    CollisionFilter(T userData) {
        this.userData = userData;
    }

    @Override
    public boolean isAllowed(Filter filter) {
        return false;
    }

    public T getUserData() {
        return userData;
    }

    public void setUserData(T userData) {
        this.userData = userData;
    }
}
