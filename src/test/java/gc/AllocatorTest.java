package gc;

import static org.junit.Assert.*;

import objects.NullHeapException;
import objects.episcopal.Int;
import org.junit.Before;
import org.junit.Test;

public class AllocatorTest {

    private Allocator allocator;

    @Before
    public void before() {
        allocator = new Allocator();
    }

    @Test
    public void testNewAllocatorState() {
        assertEquals(allocator.heapSize(), allocator.freeSpace());
        assertEquals(1, allocator.freeRegions());
        assertEquals(0, allocator.allocatedObjects());
    }

    @Test
    public void testBasicAllocation() throws OutOfMemoryException, NullHeapException {
        Int a = new Int();
        int freeBefore = allocator.freeSpace();
        allocator.allocate(a);
        int freeAfter = allocator.freeSpace();
        a.setValue(10);
        assertEquals(a.size(), freeBefore - freeAfter);
        assertEquals(10, a.getValue());
    }

    @Test
    public void testBasicAllocationAndFree() throws OutOfMemoryException, NullHeapException {
        Int a = new Int();
        int freeBefore = allocator.freeSpace();
        allocator.allocate(a);
        int freeAfter = allocator.freeSpace();
        a.setValue(10);
        assertEquals(a.size(), freeBefore - freeAfter);
        int freeRegions = allocator.freeRegions();
        freeBefore = allocator.freeSpace();
        allocator.free(a);
        freeAfter = allocator.freeSpace();
        assertEquals(freeAfter, freeBefore + a.size());
        assertEquals(freeRegions + 1, allocator.freeRegions());
        assertEquals(allocator.heapSize(), allocator.freeSpace());
    }

    @Test
    public void testValueGetAfterFree() throws OutOfMemoryException, NullHeapException {
        Int a = new Int();
        allocator.allocate(a);
        a.setValue(42);
        assertEquals(42, a.getValue());
        allocator.free(a);
        try {
            assertEquals(0, a.getValue());
            fail("expected NullHeapException when accessing property value after freeing");
        } catch (NullHeapException e) {
            // we expect this, do nothing...
        }
    }

    @Test
    public void testValueSetAfterFree() throws OutOfMemoryException, NullHeapException {
        Int a = new Int();
        allocator.allocate(a);
        a.setValue(42);
        assertEquals(42, a.getValue());
        allocator.free(a);
        try {
            a.setValue(60);
            fail("expected NullHeapException when mutating property value after freeing");
        } catch (NullHeapException e) {
            // we expect this, do nothing...
        }
    }
}
