package object.properties;

import object.management.MemoryManagedObject;
import object.management.NullHeapException;

public class ReferenceProperty<T extends MemoryManagedObject> extends IntProperty {

    private T instance;

    public ReferenceProperty(T instance) {
        this.instance = instance;
    }

    @Override
    public void set(Integer value) throws NullHeapException {
        T instance = getInstance();
        // the passed value has authority over the address in this case
        if (instance != null)
            instance.setAddress(value);
        super.set(value);
    }

    @Override
    public Integer get() throws NullHeapException {
        int address = super.get();
        T instance = getInstance();
        if (instance != null && instance.getAddress() != address) {
            // the current instance has authority over the address in this case
            // synchronise these
            address = instance.getAddress();
            super.set(address);
        }
        return address;
    }

    public T getInstance() {
        // any access of the instance synchronises the object's heap to this property's parent's heap
        if (instance != null)
            instance.setHeap(getParent().getHeap());
        return instance;
    }

    public void setInstance(T instance) throws NullHeapException {
        this.instance = instance;
        // set this property's value to the global address of the object
        set(instance.getAddress());
    }
}
