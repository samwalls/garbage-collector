package objects.episcopal;

import gc.AllocationException;
import gc.Allocator;
import gc.OutOfMemoryException;
import objects.managed.NullHeapException;
import objects.episcopal.representations.DistributionRepresentation;
import objects.managed.PropertyAccessException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DistribTest {

    private class AlternateDistributionRepresentation extends DistributionRepresentation { }

    private Allocator alloc;

    @Before
    public void setup() {
        alloc = new Allocator();
    }

    @Test
    public void testDistributionRepresentationUnmarshall() throws OutOfMemoryException, PropertyAccessException, AllocationException {
        Distrib<DistributionRepresentation> d1 = new Distrib<>(DistributionRepresentation.class, 0);
        Distrib<AlternateDistributionRepresentation> d2 = new Distrib<>(AlternateDistributionRepresentation.class, 0);
        alloc.allocate(d1);
        alloc.allocate(d2);
        assertEquals(DistributionRepresentation.class, d1.distributionType.get());
        assertEquals(AlternateDistributionRepresentation.class, d2.distributionType.get());
    }

    @Test
    public void testDistributionElementAccess() throws AllocationException, OutOfMemoryException, NullHeapException {
        Int a = new Int(), b = new Int(), c = new Int();
        Distrib<DistributionRepresentation> distrib = new Distrib<>(DistributionRepresentation.class, 3);
        // example scenario, set up concrete values for the distribution, and use the distribution elements to point to them
        alloc.allocate(a);
        alloc.allocate(b);
        alloc.allocate(c);
        a.value.set(10);
        a.value.set(20);
        a.value.set(30);
        alloc.allocate(distrib);
        distrib.elementAddress(0).set(a.getAddress());
        distrib.elementAddress(1).set(b.getAddress());
        distrib.elementAddress(2).set(c.getAddress());
        assertEquals(a.getAddress(), distrib.elementAddress(0).get().intValue());
        assertEquals(b.getAddress(), distrib.elementAddress(1).get().intValue());
        assertEquals(c.getAddress(), distrib.elementAddress(2).get().intValue());
    }
}
