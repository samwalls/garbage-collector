package gc;

import object.management.MemoryManagedObject;

import object.management.Property;
import object.management.NullHeapException;

public interface Allocator<T extends MemoryManagedObject> {

    /**
     * Ensure the given object is marked as "allocated" and is associated with a heap. Otherwise, throw an exception.
     * When a {@link MemoryManagedObject} is allocated, its properties can be safely accessed without throwing a
     * {@link NullHeapException}.
     * @param object the object to allocate and associate
     * @throws AllocationException if there was a problem allocating space for the object or associating it
     */
    void allocate(T object) throws AllocationException;

    /**
     * Ensure that the given {@link MemoryManagedObject} is unmarked as "allocated" - i.e. {@link Property}s in the passed
     * {@link MemoryManagedObject} will be inaccessible, and will throw a {@link NullHeapException} - and the object is
     * disassociated with any heap. Otherwise throw an exception.
     * @param object the object to deallocate and disassociate
     * @throws AllocationException if there was a problem freeing the space of the object, or disassociating it
     */
    void free(T object) throws AllocationException;
}
