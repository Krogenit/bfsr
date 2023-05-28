package clipper2.engine;

import clipper2.core.PathType;

final class LocalMinima {

    ClipperBase.Vertex vertex;
    PathType polytype;
    boolean isOpen;

    LocalMinima() {
    }

    LocalMinima(ClipperBase.Vertex vertex, PathType polytype) {
        this(vertex, polytype, false);
    }

    LocalMinima(ClipperBase.Vertex vertex, PathType polytype, boolean isOpen) {
        this.vertex = vertex;
        this.polytype = polytype;
        this.isOpen = isOpen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalMinima minima) {
            return this == minima;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return vertex.hashCode();
    }

    @Override
    protected LocalMinima clone() {
        LocalMinima varCopy = new LocalMinima();

        varCopy.vertex = this.vertex;
        varCopy.polytype = this.polytype;
        varCopy.isOpen = this.isOpen;

        return varCopy;
    }
}