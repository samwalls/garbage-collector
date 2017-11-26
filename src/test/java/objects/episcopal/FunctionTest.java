package objects.episcopal;

import gc.AllocationException;
import gc.Allocator;
import gc.OutOfMemoryException;
import objects.managed.NullHeapException;
import objects.episcopal.representations.ClosureRepresentation;
import objects.managed.PropertyAccessException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FunctionTest {

    private class AlternateClosureRepresentation extends ClosureRepresentation { }

    private Allocator alloc;

    @Before
    public void setup() {
        alloc = new Allocator();
    }

    @Test
    public void testClosureRepresentationUnmarshall() throws OutOfMemoryException, PropertyAccessException, AllocationException {
        Function<ClosureRepresentation> f1 = new Function<>(ClosureRepresentation.class, 0);
        Function<AlternateClosureRepresentation> f2 = new Function<>(AlternateClosureRepresentation.class, 0);
        alloc.allocate(f1);
        alloc.allocate(f2);
        assertEquals(ClosureRepresentation.class, f1.closureType.get());
        assertEquals(AlternateClosureRepresentation.class, f2.closureType.get());
    }

    @Test
    public void testDistributionElementAccess() throws AllocationException, OutOfMemoryException, NullHeapException {
        Int a = new Int(), b = new Int(), c = new Int();
        Function<ClosureRepresentation> function = new Function<>(ClosureRepresentation.class, 3);
        // example scenario, set up concrete values for the distribution, and use the distribution elements to point to them
        alloc.allocate(a);
        alloc.allocate(b);
        alloc.allocate(c);
        a.value.set(10);
        b.value.set(20);
        c.value.set(30);
        alloc.allocate(function);
        function.paramAddress(0).set(a.getAddress());
        function.paramAddress(1).set(b.getAddress());
        function.paramAddress(2).set(c.getAddress());
        assertEquals(a.getAddress(), function.paramAddress(0).get().intValue());
        assertEquals(b.getAddress(), function.paramAddress(1).get().intValue());
        assertEquals(c.getAddress(), function.paramAddress(2).get().intValue());
    }
}
