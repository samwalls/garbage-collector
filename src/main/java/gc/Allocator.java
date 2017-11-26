package gc;

import object.management.MemoryManagedObject;

public interface Allocator<T extends MemoryManagedObject> {

    void allocate(T object) throws AllocationException;

    void free(T object) throws AllocationException;
}
