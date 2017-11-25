package gc;

import static org.junit.Assert.*;
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
    public void testBasicAllocation() {
        Int a = new Int(0);
        Int b = new Int(1);
        Int c = new Int(2);
        allocator.allocate(a);
        allocator.allocate(b);
        allocator.allocate(c);
        a.setValue(10);
        b.setValue(20);
        c.setValue(30);
        assertEquals(10, a.getValue().intValue());
        assertEquals(20, b.getValue().intValue());
        assertEquals(30, c.getValue().intValue());
    }
}
