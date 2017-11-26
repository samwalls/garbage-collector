package object.management;

import object.Marshallable;
import object.Sizeable;

public abstract class GeneralProperty<T> implements Marshallable<T>, Sizeable {

    private int address;

    private MemoryManagedObject parent;

    void setParent(MemoryManagedObject parent) {
        this.parent = parent;
    }

    MemoryManagedObject getParent() {
        return parent;
    }

    int getRelativeAddress() {
        return address;
    }

    void setRelativeAddress(int address) {
        this.address = address;
    }
}
