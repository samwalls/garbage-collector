package episcopal;

import gc.AllocationException;
import gc.BasicAllocator;
import object.management.NullHeapException;
import episcopal.representations.DistributionRepresentation;
import object.management.PropertyAccessException;
import object.properties.ReferenceProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DistribTest {

    private class AlternateDistributionRepresentation extends DistributionRepresentation { }

    private BasicAllocator alloc;

    @Before
    public void setup() {
        alloc = new BasicAllocator();
    }

    @Test
    public void testDistributionRepresentationUnmarshall() throws PropertyAccessException, AllocationException {
        Distrib<DistributionRepresentation> d1 = new Distrib<>(DistributionRepresentation.class, 0);
        Distrib<AlternateDistributionRepresentation> d2 = new Distrib<>(AlternateDistributionRepresentation.class, 0);
        alloc.allocate(d1);
        alloc.allocate(d2);
        assertEquals(DistributionRepresentation.class, d1.distributionType.get());
        assertEquals(AlternateDistributionRepresentation.class, d2.distributionType.get());
    }

    @Test
    public void testDistributionElementAccess() throws AllocationException, NullHeapException {
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

    @Test
    public void testDistriubtionReachableLinks() throws AllocationException, NullHeapException {
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
        // set the types and values of the distribution element pointers
        distrib.elementAddress(0).setInstance(new Int());
        distrib.elementAddress(0).set(a.getAddress());
        distrib.elementAddress(1).setInstance(new Int());
        distrib.elementAddress(1).set(b.getAddress());
        distrib.elementAddress(2).setInstance(new Int());
        distrib.elementAddress(2).set(c.getAddress());
        // check the set of reachable links from the distribution
        List<ReferenceProperty> references = distrib.reachableReferences();
        assertEquals(3, references.size());
        for (ReferenceProperty o : references)
            assertTrue(o.getInstance() instanceof Int);
    }
}
