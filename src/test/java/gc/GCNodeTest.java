package gc;

import static org.junit.Assert.*;

import episcopal.EpiscopalObject;
import episcopal.Int;
import object.management.PropertyAccessException;
import org.junit.Before;
import org.junit.Test;

public class GCNodeTest {

    private BasicAllocator alloc;

    @Before
    public void setup() {
        alloc = new BasicAllocator();
    }

    @Test
    public void testNodeStateWithNoPrevOrNext() throws AllocationException, PropertyAccessException {
        // create an int on the heap
        Int data = new Int();
        alloc.allocate(data);
        data.value.set(42);
        // nodes need to keep track of an instance of their data type which they can provide back to the code user
        GCNode<EpiscopalObject> node = new GCNode<>(new Int());
        alloc.allocate(node);
        // make the node point to the aforementioned int
        node.data.set(data.getAddress());
        assertNotNull(node.data.getInstance());
        assertTrue("expected node's instance data to be a subclass of Int", node.data.getInstance() instanceof Int);
        assertEquals(data.value.get(), ((Int)node.data.getInstance()).value.get());
        assertNull(node.prev.getInstance());
        assertNull(node.next.getInstance());
    }

    @Test
    public void testAddPrevAndNextNodesAfter() throws AllocationException, PropertyAccessException {
        Int data = new Int();
        Int nextData = new Int();
        Int prevData = new Int();
        GCNode<Int> node = new GCNode<>(new Int());
        GCNode<Int> prev = new GCNode<>(new Int());
        GCNode<Int> next = new GCNode<>(new Int());
        alloc.allocate(data);
        alloc.allocate(nextData);
        alloc.allocate(prevData);
        alloc.allocate(node);
        alloc.allocate(prev);
        alloc.allocate(next);
        // put some values into the heap
        data.value.set(42);
        prevData.value.set(100);
        nextData.value.set(200);
        // make the nodes point to their respective data
        node.data.set(data.getAddress());
        prev.data.set(prevData.getAddress());
        next.data.set(nextData.getAddress());
        // set prev and next on the node
        node.prev.setInstance(prev);
        node.next.setInstance(next);
        assertNotNull(node.data.getInstance());
        assertNotNull(node.next.getInstance());
        assertNotNull(node.prev.getInstance());
        assertEquals(prev.getAddress(), node.prev.get().intValue());
        assertEquals(next.getAddress(), node.next.get().intValue());
        assertEquals(prevData.value.get(), ((Int)node.prev.getInstance().data.getInstance()).value.get());
        assertEquals(nextData.value.get(), ((Int)node.next.getInstance().data.getInstance()).value.get());
    }

    @Test
    public void testPrevAndNextOnConstruction() throws AllocationException, PropertyAccessException {
        Int data = new Int();
        Int nextData = new Int();
        Int prevData = new Int();
        GCNode<Int> prev = new GCNode<>(new Int());
        GCNode<Int> next = new GCNode<>(new Int());
        GCNode<Int> node = new GCNode<>(prev, next, new Int());
        alloc.allocate(data);
        alloc.allocate(nextData);
        alloc.allocate(prevData);
        alloc.allocate(node);
        alloc.allocate(prev);
        alloc.allocate(next);
        // put some values into the heap
        data.value.set(42);
        prevData.value.set(100);
        nextData.value.set(200);
        // make the nodes point to their respective data
        node.data.set(data.getAddress());
        prev.data.set(prevData.getAddress());
        next.data.set(nextData.getAddress());
        assertNotNull(node.data.getInstance());
        assertNotNull(node.next.getInstance());
        assertNotNull(node.prev.getInstance());
        assertEquals(prev, node.prev.getInstance());
        assertEquals(next, node.next.getInstance());
        assertEquals(prev.getAddress(), node.prev.get().intValue());
        assertEquals(next.getAddress(), node.next.get().intValue());
        assertEquals(prevData.value.get(), ((Int)node.prev.getInstance().data.getInstance()).value.get());
        assertEquals(nextData.value.get(), ((Int)node.next.getInstance().data.getInstance()).value.get());
    }

    @Test
    public void testLinkedListIteration() throws AllocationException, PropertyAccessException {
        int iterations = 10;
        GCNode<? extends EpiscopalObject> root = null;
        GCNode<Int> previous = null;
        // put values on the heap
        for (int i = 0; i < iterations; i++) {
            Int data = new Int();
            // create an int at position i in memory
            alloc.allocate(data);
            data.value.set(10 * (i + 1));
        }
        // set up the linked list of nodes
        for (int i = 0; i < iterations; i++) {
            GCNode<Int> node = new GCNode<>(new Int());
            alloc.allocate(node);
            // set this node's data pointing to position i
            node.data.set(i);
            if (i == 0)
                root = node;
            // link the previous node to this one
            if (previous != null)
                previous.next.setInstance(node);
            previous = node;
        }
        assertNotNull(root);
        // we should find by iterating over the nodes a list of references to values 10, 20, 30, etc...
        GCNode<? extends EpiscopalObject> node = root;
        for (int i = 0; i < iterations; i++) {
            assertTrue(node.data.getInstance() instanceof Int);
            assertEquals(10 * (i + 1), ((Int)node.data.getInstance()).value.get().intValue());
            node = node.next.getInstance();
        }
    }
}
