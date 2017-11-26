package object.management;

import object.Marshallable;
import object.Sizeable;

public abstract class GeneralProperty<T> implements Marshallable<T>, Sizeable {

    private int relativeAddress;

    private MemoryManagedObject parent;

    void setParent(MemoryManagedObject parent) {
        this.parent = parent;
    }

    protected MemoryManagedObject getParent() {
        return parent;
    }

    int getRelativeAddress() {
        return relativeAddress;
    }

    void setRelativeAddress(int address) {
        this.relativeAddress = address;
    }
}
