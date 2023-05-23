package clipper2.engine;

import clipper2.core.PathType;
import clipper2.engine.ClipperBase.Vertex;

final class LocalMinima {

    Vertex vertex;
    PathType polytype;
    boolean isOpen;

    LocalMinima() {
    }

    LocalMinima(Vertex vertex, PathType polytype) {
        this(vertex, polytype, false);
    }

    LocalMinima(Vertex vertex, PathType polytype, boolean isOpen) {
        this.vertex = vertex;
        this.polytype = polytype;
        this.isOpen = isOpen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalMinima) {
            LocalMinima minima = (LocalMinima) obj;
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