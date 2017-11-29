package profiling;

import episcopal.Indirect;
import gc.AllocationException;
import gc.DebugMode;
import gc.TreadmillAllocator;
import object.management.NullHeapException;

import java.util.Arrays;

public class TreadmillIndirectProblem extends Problem {

    private TreadmillAllocator allocator;

    private Indirect<Indirect> rootObject;

    @Override
    public void init(int size) throws AllocationException, NullHeapException {
        rootObject = new Indirect<>(null);
        allocator = new TreadmillAllocator(Arrays.asList(rootObject), DebugMode.NONE);
        Indirect<Indirect> last = rootObject;
        for (int i = 0; i < size; i++) {
            Indirect<Indirect> next = new Indirect<>(null);
            last.value.setInstance(next);
            allocator.allocate(next);
            last = next;
        }
//        allocator.setDebugMode(DebugMode.BASIC);
    }

    @Override
    public void run() throws AllocationException {
        // free the the object the root points to, all the other indirects which that points to should also get freed
        allocator.free(rootObject.value.getInstance());
    }
}
