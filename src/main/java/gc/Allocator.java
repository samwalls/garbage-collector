package gc;

import objects.managed.MemoryManagedObject;
import objects.managed.PropertyAccessException;

import java.util.HashSet;
import java.util.Set;

public class Allocator {

    private Heap heap;

    private Set<MemoryManagedObject> objects;

    private FreeRegion freeRoot;

    public Allocator() {
        heap = new Heap();
        objects = new HashSet<>();
        freeRoot = new FreeRegion(0, heap.getSize(), null);
    }

    public void allocate(MemoryManagedObject object) throws OutOfMemoryException, AllocationException {
        // TODO free region coalescing
        // find the first free region with enough space to allocate the object
        FreeRegion previous = null, current = freeRoot;
        while (current != null && current.getSize() < object.size()) {
            previous = current;
            current = current.getNext();
        }
        // if we reached the end and there was no space
        if (current == null)
            throw new OutOfMemoryException("no space to allocate object \"" + object.toString() + "\" of size " + object.size() + " to heap");
        int difference = current.getSize() - object.size();
        object.setAddress(current.getAddress());
        // if the difference is zero then we need to unlink this node from the chain
        // otherwise simply reassign its address and size
        if (difference <= 0) {
            if (previous == null) {
                // forget about current, reassign the root
                freeRoot = current.getNext();
            } else {
                // make the previous free region the same as next, and forget about next
                previous.setNext(current.getNext());
            }
        } else {
            current.setAddress(current.getAddress() + object.size());
            current.setSize(difference);
        }
        object.setHeap(heap);
        objects.add(object);
        // perform onAllocate behaviour if present
        try {
            object.onAllocate();
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    public void free(MemoryManagedObject object) {
        // always link the current free root as the new free root's next
        // thus the unused space to the right of the last object is always at the end of the path
        freeRoot = new FreeRegion(object.getAddress(), object.size(), freeRoot);
        object.setHeap(null);
        objects.remove(object);
    }

    public int allocatedObjects() {
        return objects.size();
    }

    public int freeRegions() {
        int length = 0;
        FreeRegion current = freeRoot;
        while (current != null) {
            length++;
            current = current.getNext();
        }
        return length;
    }

    public int freeSpace() {
        int space = 0;
        FreeRegion current = freeRoot;
        while (current != null) {
            space += current.getSize();
            current = current.getNext();
        }
        return space;
    }

    public int heapSize() {
        return heap.getSize();
    }
}
