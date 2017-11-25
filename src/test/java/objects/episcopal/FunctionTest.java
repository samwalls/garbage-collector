package objects.episcopal;

import gc.AllocationException;
import gc.Allocator;
import gc.OutOfMemoryException;
import objects.NullHeapException;
import objects.episcopal.representations.ClosureRepresentation;
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
    public void testClosureRepresentationUnmarshall() throws OutOfMemoryException, NullHeapException, AllocationException {
        Function<ClosureRepresentation> f1 = new Function<>(ClosureRepresentation.class, 0);
        Function<AlternateClosureRepresentation> f2 = new Function<>(AlternateClosureRepresentation.class, 0);
        alloc.allocate(f1);
        alloc.allocate(f2);
        assertEquals(ClosureRepresentation.class, f1.getClosure());
        assertEquals(AlternateClosureRepresentation.class, f2.getClosure());
    }

    @Test
    public void testDistributionElementAccess() throws AllocationException, OutOfMemoryException, NullHeapException {
        Int a = new Int(), b = new Int(), c = new Int();
        Function<ClosureRepresentation> function = new Function<>(ClosureRepresentation.class, 3);
        // example scenario, set up concrete values for the distribution, and use the distribution elements to point to them
        alloc.allocate(a);
        alloc.allocate(b);
        alloc.allocate(c);
        a.setValue(10);
        a.setValue(20);
        a.setValue(30);
        alloc.allocate(function);
        function.setParamAddress(0, a.getAddress());
        function.setParamAddress(1, b.getAddress());
        function.setParamAddress(2, c.getAddress());
        assertEquals(a.getAddress(), function.getParamAddress(0));
        assertEquals(b.getAddress(), function.getParamAddress(1));
        assertEquals(c.getAddress(), function.getParamAddress(2));
    }
}
