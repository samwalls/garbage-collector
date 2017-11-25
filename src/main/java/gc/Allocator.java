package gc;

import objects.MemoryManagedObject;

import java.util.HashSet;
import java.util.Set;

public class Allocator {

    private Heap heap;

    private Set<MemoryManagedObject> objects;

    public Allocator() {
        heap = new Heap();
        objects = new HashSet<>();
    }

    public void allocate(MemoryManagedObject object) {
        object.setHeap(heap);
        objects.add(object);
    }

    public void free(MemoryManagedObject object) {
        object.setHeap(null);
        objects.remove(object);
    }
}
