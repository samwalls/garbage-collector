package gc;

import episcopal.Function;
import episcopal.Indirect;
import episcopal.Int;
import episcopal.representations.ClosureRepresentation;
import object.management.PropertyAccessException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TreadmillAllocatorTest {

    private TreadmillAllocator test1;

    private Function<ClosureRepresentation> test1RootFunction;

    private void setupTest1() throws AllocationException {
        test1RootFunction = new Function<>(ClosureRepresentation.class, 4);
        test1 = new TreadmillAllocator(1, Arrays.asList(test1RootFunction), DebugMode.NONE);
    }

    @Before
    public void setup() throws AllocationException {
        setupTest1();
    }

    @Test
    public void testBasicAllocationAndFree() throws AllocationException, PropertyAccessException {
        // set up values to be pointed to by pointers
        Int value1, value2, value3, value4;
        value1 = new Int();
        value2 = new Int();
        value3 = new Int();
        value4 = new Int();
        // set up some pointers to the values; the root function will point to these pointers in turn
        Indirect<Int> arg1, arg2, arg3, arg4;
        arg1 = new Indirect<>();
        arg2 = new Indirect<>();
        arg3 = new Indirect<>();
        arg4 = new Indirect<>();
        // allocate the pointers, and make the root function point to them immediately, otherwise they will be marked unreachable and collected
        int usedNodesBefore = test1.countNonWhiteNodes();
        test1.allocate(arg1);
        test1RootFunction.paramAddress(0).setInstance(arg1);
        test1.allocate(arg2);
        test1RootFunction.paramAddress(1).setInstance(arg2);
        test1.allocate(arg3);
        test1RootFunction.paramAddress(2).setInstance(arg3);
        test1.allocate(arg4);
        int usedNodesAfter = test1.countNonWhiteNodes();
        assertEquals("expected number of GC nodes to increase after allocating objects", usedNodesBefore + 4, usedNodesAfter);
        usedNodesBefore = usedNodesAfter;
        test1RootFunction.paramAddress(3).setInstance(arg4);
        // allocate the values, and make the pointers point to them immediately
        test1.allocate(value1);
        arg1.value.setInstance(value1);
        test1.allocate(value2);
        arg2.value.setInstance(value2);
        test1.allocate(value3);
        arg3.value.setInstance(value3);
        test1.allocate(value4);
        arg4.value.setInstance(value4);
        usedNodesAfter = test1.countNonWhiteNodes();
        assertEquals("expected number of GC nodes in use to increase after allocating objects", usedNodesBefore + 4, usedNodesAfter);
        usedNodesBefore = usedNodesAfter;
        // freeing the pointers arg1, arg2, etc. should cause a garbage collection of value1, value2, etc.
        test1.free(arg1);
        test1.free(arg2);
        test1.free(arg3);
        test1.free(arg4);
        usedNodesAfter = test1.countNonWhiteNodes();
        assertEquals("expected number of GC nodes in use to decrease after freeing objects with other allocated references", usedNodesBefore - 8, usedNodesAfter);
    }
}
