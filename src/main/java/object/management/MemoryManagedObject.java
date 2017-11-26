package object.management;

import gc.Heap;
import object.Sizeable;

import java.util.ArrayList;
import java.util.List;

public abstract class MemoryManagedObject implements Sizeable {

    private Heap heap;

    /**
     * Where this object resides in a given heap.
     */
    private int address;

    /**
     * nextRelativePropertyAddress is incremented as properties are added at object construction time - it also
     * represents the size of this MemoryManagedObject.
     */
    private int nextRelativePropertyAddress;

    /**
     * A map from property to an address relative to this object
     */
    private List<GeneralProperty> properties;

    public MemoryManagedObject(int address) {
        this.address = address;
        this.nextRelativePropertyAddress = 0;
        this.properties = new ArrayList<>();
    }

    public MemoryManagedObject(Integer address) {
        this(address.intValue());
    }

    public MemoryManagedObject() {
        this(0);
    }

    /**
     * Behaviour which is inserted directly after allocation. Useful to insert marshalling behaviour of constants
     * (cannot be reset after allocation).
     */
    public void onAllocate() throws PropertyAccessException { }

    public int size() {
        return nextRelativePropertyAddress;
    }

    /**
     * Set the heap to marshall / unmarshall properties from.
     * @param heap the heap to use for marshalling / unmarshalling properties
     */
    public void setHeap(Heap heap) {
        this.heap = heap;
    }

    public Heap getHeap() {
        return heap;
    }

    public int getAddress() {
        return address;
    }

    /**
     * Package-local property setter for changing the address of this object.
     * @param address the address of this object in the heap
     */
    public void setAddress(int address) {
        this.address = address;
    }

    protected <T> T get(GeneralProperty<T> property) throws NullHeapException {
        return property.unmarshall(readForProperty(property));
    }

    protected <T> void set(GeneralProperty<T> property, T value) throws NullHeapException {
        writeForProperty(property, property.marshall(value));
    }

    protected void addProperty(GeneralProperty property) {
        if (properties.contains(property))
            throw new RuntimeException("property \"" + property.toString() + "\" already exists");
        property.setRelativeAddress(nextRelativePropertyAddress);
        properties.add(property);
        property.setParent(this);
        nextRelativePropertyAddress += property.size();
    }

    private long[] readForProperty(GeneralProperty property) throws NullHeapException {
        checkHeap(property);
        int absolute = address + property.getRelativeAddress();
        return heap.get(absolute, property.size());
    }

    private void writeForProperty(GeneralProperty property, long[] data) throws NullHeapException {
        checkHeap(property);
        int absolute = address + property.getRelativeAddress();
        heap.put(absolute, data);
    }

    private void checkHeap(GeneralProperty p) throws NullHeapException {
        if (heap == null)
            throw new NullHeapException("memory managed object \"" + this.toString() + "\" is not associated with a heap to view property \"" + p.toString() + "\"");
    }
}
