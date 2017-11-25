package objects;

import gc.Heap;
import objects.properties.Property;

import java.util.HashMap;
import java.util.Map;

public class MemoryManagedObject implements Sizeable {

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
    private Map<Property, Integer> properties;

    public MemoryManagedObject(int address) {
        this.address = address;
        this.nextRelativePropertyAddress = 0;
        this.properties = new HashMap<>();
    }

    public MemoryManagedObject() {
        this(0);
    }

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

    protected long[] readForProperty(Property property) throws NullHeapException {
        checkHeap(property);
        int absolute = address + relativePropertyAddress(property);
        return heap.get(absolute, property.size());
    }

    protected <T> void writeForProperty(Property<T> property, long[] data) throws NullHeapException {
        checkHeap(property);
        int absolute = address + relativePropertyAddress(property);
        heap.put(absolute, data);
    }

    protected void addProperty(Property property) {
        if (properties.containsKey(property))
            throw new RuntimeException("property \"" + property.toString() + "\" already exists");
        properties.put(property, nextRelativePropertyAddress);
        nextRelativePropertyAddress += property.size();
    }

    protected int relativePropertyAddress(Property p) {
        if (p == null)
            throw new PropertyNotFoundException("cannot use null property");
        if (!properties.containsKey(p))
            throw new PropertyNotFoundException("property \"" + p.toString() + "\" does not exist");
        return properties.get(p);
    }

    private void checkHeap(Property p) throws NullHeapException {
        if (heap == null)
            throw new NullHeapException("memory managed object \"" + this.toString() + "\" is not associated with a heap to view property \"" + p.toString() + "\"");
    }
}
