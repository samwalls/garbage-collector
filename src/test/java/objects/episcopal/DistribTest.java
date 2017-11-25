package objects.episcopal;

import gc.AllocationException;
import gc.Allocator;
import gc.OutOfMemoryException;
import objects.NullHeapException;
import objects.episcopal.representations.DistributionRepresentation;
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
    public void testDistributionRepresentationUnmarshall() throws OutOfMemoryException, NullHeapException, AllocationException {
        Distrib<DistributionRepresentation> d1 = new Distrib<>(DistributionRepresentation.class, 0);
        Distrib<AlternateDistributionRepresentation> d2 = new Distrib<>(AlternateDistributionRepresentation.class, 0);
        alloc.allocate(d1);
        alloc.allocate(d2);
        assertEquals(DistributionRepresentation.class, d1.getDistribType());
        assertEquals(AlternateDistributionRepresentation.class, d2.getDistribType());
    }

    @Test
    public void testDistributionElementAccess() throws AllocationException, OutOfMemoryException, NullHeapException {
        Int a = new Int(), b = new Int(), c = new Int();
        Distrib<DistributionRepresentation> distrib = new Distrib<>(DistributionRepresentation.class, 3);
        // example scenario, set up concrete values for the distribution, and use the distribution elements to point to them
        alloc.allocate(a);
        alloc.allocate(b);
        alloc.allocate(c);
        a.setValue(10);
        a.setValue(20);
        a.setValue(30);
        alloc.allocate(distrib);
        distrib.setElementAddress(0, a.getAddress());
        distrib.setElementAddress(1, b.getAddress());
        distrib.setElementAddress(2, c.getAddress());
        assertEquals(a.getAddress(), distrib.getElementAddress(0));
        assertEquals(b.getAddress(), distrib.getElementAddress(1));
        assertEquals(c.getAddress(), distrib.getElementAddress(2));
    }
}
