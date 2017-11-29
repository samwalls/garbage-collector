package profiling;

import episcopal.Indirect;
import gc.DebugMode;
import gc.TreadmillAllocator;

import java.util.Arrays;

public class TreadmillInsertionProblem extends Problem {

    private int size;

    @Override
    public void init(int size) throws Exception {
        this.size = size;
    }

    @Override
    public void run() throws Exception {
        Indirect rootObject = new Indirect<>(null);
        TreadmillAllocator allocator = new TreadmillAllocator(Arrays.asList(rootObject), DebugMode.NONE);
        Indirect<Indirect> last = rootObject;
        for (int i = 0; i < size; i++) {
            Indirect<Indirect> next = new Indirect<>(null);
            last.value.setInstance(next);
            allocator.allocate(next);
            last = next;
        }
    }
}
