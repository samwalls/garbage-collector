package gc;

import static org.junit.Assert.*;

import object.management.NullHeapException;
import gc.episcopal.Distrib;
import gc.episcopal.Int;
import gc.episcopal.representations.DistributionRepresentation;
import org.junit.Before;
import org.junit.Test;

public class BasicAllocatorTest {

    private BasicAllocator allocator;

    @Before
    public void before() {
        allocator = new BasicAllocator();
    }

    @Test
    public void testNewAllocatorState() {
        assertEquals(allocator.heapSize(), allocator.freeSpace());
        assertEquals(1, allocator.freeRegions());
        assertEquals(0, allocator.allocatedObjects());
    }

    @Test
    public void testBasicAllocation() throws NullHeapException, AllocationException {
        Int a = new Int();
        int freeBefore = allocator.freeSpace();
        allocator.allocate(a);
        int freeAfter = allocator.freeSpace();
        a.value.set(10);
        assertEquals(a.size(), freeBefore - freeAfter);
        assertEquals(10, a.value.get().intValue());
    }

    @Test
    public void testBasicAllocationAndFree() throws NullHeapException, AllocationException {
        Int a = new Int();
        int freeBefore = allocator.freeSpace();
        allocator.allocate(a);
        int freeAfter = allocator.freeSpace();
        a.value.set(10);
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
    public void testValueGetAfterFree() throws NullHeapException, AllocationException {
        Int a = new Int();
        allocator.allocate(a);
        a.value.set(42);
        assertEquals(42, a.value.get().intValue());
        allocator.free(a);
        try {
            assertEquals(0, a.value.get().intValue());
            fail("expected NullHeapException when accessing property value after freeing");
        } catch (NullHeapException e) {
            // we expect this, do nothing...
        }
    }

    @Test
    public void testValueSetAfterFree() throws NullHeapException, AllocationException {
        Int a = new Int();
        allocator.allocate(a);
        a.value.set(42);
        assertEquals(42, a.value.get().intValue());
        allocator.free(a);
        try {
            a.value.set(60);
            fail("expected NullHeapException when mutating property value after freeing");
        } catch (NullHeapException e) {
            // we expect this, do nothing...
        }
    }

    @Test
    public void testAllocationIntoFreeRegionWithExactSpace() throws NullHeapException, AllocationException {
        Int a = new Int();
        allocator.allocate(a);
        a.value.set(42);
        int freeRegionsBefore = allocator.freeRegions();
        allocator.free(a);
        int freeRegionsAfter = allocator.freeRegions();
        assertEquals("expected free regions to increase by one after freeing allocated object", freeRegionsAfter, freeRegionsBefore + 1);
        freeRegionsBefore = freeRegionsAfter;
        allocator.allocate(a);
        freeRegionsAfter = allocator.freeRegions();
        assertEquals("expected free regions to decrease by one after allocating object with enough space to occupy it completely", freeRegionsAfter, freeRegionsBefore - 1);
    }

    @Test
    public void testAllocationIntoFreeRegionWithMoreSpace() throws OutOfMemoryException, NullHeapException, AllocationException {
        Distrib<DistributionRepresentation> distrib = new Distrib<>(DistributionRepresentation.class, 5);
        Int integer = new Int();
        assertTrue("distribution object with 5 elements should take more space than a single integer", distrib.size() > integer.size());
        allocator.allocate(distrib);
        int freeRegionsBefore = allocator.freeRegions();
        allocator.free(distrib);
        int freeRegionsAfter = allocator.freeRegions();
        assertEquals("expected free regions to increase by one after freeing allocated object", freeRegionsAfter, freeRegionsBefore + 1);
        freeRegionsBefore = freeRegionsAfter;
        allocator.allocate(integer);
        freeRegionsAfter = allocator.freeRegions();
        assertEquals("expected there to be same number of free regions after allocating into a free region with supplementary space", freeRegionsAfter, freeRegionsBefore);
        freeRegionsBefore = freeRegionsAfter;
        allocator.free(integer);
        freeRegionsAfter = allocator.freeRegions();
        assertEquals("expected there to be more free regions after freeing an object which divided a free region through allocation", freeRegionsAfter, freeRegionsBefore + 1);
    }
}
