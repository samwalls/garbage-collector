package gc;

import gc.episcopal.EpiscopalObject;
import object.management.MemoryManagedObject;
import object.management.NullHeapException;
import object.management.PropertyAccessException;
import object.properties.ReferenceProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TreadmillAllocator implements Allocator<EpiscopalObject> {

    private final int scanFrequency;
    private int currentScan = 0;

    private Set<GCNode<? super EpiscopalObject>> nodes;

    private Set<EpiscopalObject> roots;

    private BasicAllocator heapAllocator;

    // top <-> ... grey nodes ... <-> scan
    // scan <-> ... black nodes ... <-> free
    // free <-> ... white nodes ... <-> bottom
    // bottom <-> ... ecru (off-white) nodes ... <-> top
    private GCNode<? super EpiscopalObject> top, scan, free, bottom;

    public TreadmillAllocator(int heapSize, int scanFrequency, Collection<EpiscopalObject> roots) throws AllocationException {
        if (scanFrequency <= 0)
            throw new IllegalArgumentException("illegal scan frequency: " + scanFrequency + "; frequency must be > 0");
        this.scanFrequency = scanFrequency;
        heapAllocator = new BasicAllocator(heapSize);
        try {
            initTreadmill(roots);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    public TreadmillAllocator(int scanFrequency, Collection<EpiscopalObject> roots) throws AllocationException {
        this(BasicAllocator.HEAP_SIZE_DEFAULT, scanFrequency, roots);
    }

    //******** ALLOCATOR IMPLEMENTATION ********//

    @Override
    public void allocate(EpiscopalObject object) throws AllocationException {
        printTreadmill("before allocation");
        // scan if necessary
        if (++currentScan >= scanFrequency) {
            scan();
            currentScan = 0;
        }
        // flip if there are no more free slots
        if (isHeapFull()) {
            flip();
            if (isHeapFull())
                throw new OutOfMemoryException("heap is full");
        }
        allocateObjectIntoFree(object);
        printTreadmill("after allocation");
    }

    @Override
    public void free(EpiscopalObject object) throws AllocationException {
        GCNode<? super EpiscopalObject> node = object.getGCNode();
        // TODO
    }

    //******** NODE HELPERS ********//

    private boolean isHeapFull() {
        return free == bottom;
    }

    private void allocateObjectIntoFree(EpiscopalObject object) throws AllocationException {
        heapAllocator.allocate(object);
        object.setGCNode(free);
        try {
            free.data.setInstance(object);
            free.setType(NodeType.BLACK);
            free = free.next.getInstance();
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    private void unlink(GCNode<? super EpiscopalObject> node) throws NullHeapException {
        GCNode<? super EpiscopalObject> prev = node.prev.getInstance();
        GCNode<? super EpiscopalObject> next = node.prev.getInstance();
        if (prev != null)
            prev.next.setInstance(next);
        if (next != null)
            next.prev.setInstance(prev);
        if (top == node)
            top = next;
        if (scan == node)
            scan = next;
        if (free == node)
            free = next;
        if (bottom == node)
            bottom = next;
    }

    /**
     * Insert prev to the left of node, unlinking it from it's current position.
     * @param node the target node
     * @param prev the node to insert as node's prev
     */
    private void insertPrev(GCNode<? super EpiscopalObject> node, GCNode<? super EpiscopalObject> prev) throws NullHeapException {
        // we don't need to do anything if this is the case
        if (node == prev)
            return;
        unlink(prev);
        // general case
        GCNode<? super EpiscopalObject> lastPrev = node.prev.getInstance();
        // make surrounding nodes point to the inserted one
        lastPrev.next.setInstance(prev);
        node.prev.setInstance(prev);
        // make the inserted node point to the surrounding nodes
        prev.prev.setInstance(lastPrev);
        prev.next.setInstance(node);
        // make sure to move around these special values if they appear
        if (node == top)
            top = prev;
        if (node == scan)
            scan = prev;
        if (node == free)
            free = prev;
        if (node == bottom)
            bottom = prev;
    }

    private void makeEcru(GCNode<? super EpiscopalObject> node) throws AllocationException {
        try {
            if (node == bottom) {
                if (node == top)
                    top = node.next.getInstance();
                if (node == scan)
                    scan = node.next.getInstance();
                if (node == free)
                    free = node.next.getInstance();
            } else {
                insertPrev(bottom, node);
            }
            node.setType(NodeType.ECRU);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    private void makeGrey(GCNode<? super EpiscopalObject> node) throws AllocationException {
        try {
            insertPrev(top, node);
            node.setType(NodeType.GREY);
            if (node == scan)
                scan = node.next.getInstance();
            if (node == free)
                free = node.next.getInstance();
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    //******** TREADMILL HELPERS ********//

    private void initTreadmill(Collection<EpiscopalObject> initialRoots) throws AllocationException, PropertyAccessException {
        roots = new HashSet<>(initialRoots);
        nodes = new HashSet<>();
        GCNode<? super EpiscopalObject> firstRoot = null;
        for (EpiscopalObject o : initialRoots) {
            GCNode<? super EpiscopalObject> node = allocateRootObject(o);
            if (firstRoot == null)
                firstRoot = node;
            if (firstRoot.prev.getInstance() == null)
                firstRoot.prev.setInstance(node);
            else
                firstRoot.prev.getInstance().next.setInstance(node);
            node.next.setInstance(firstRoot);
        }
        top = firstRoot;
        scan = firstRoot;
        free = firstRoot;
        bottom = firstRoot;
    }

    private GCNode<? super EpiscopalObject> allocateRootObject(EpiscopalObject object) throws AllocationException, PropertyAccessException {
        GCNode<? super EpiscopalObject> node = new GCNode<>(object);
        object.setGCNode(node);
        heapAllocator.allocate(object);
        heapAllocator.allocate(node);
        node.setType(NodeType.WHITE);
        nodes.add(node);
        return node;
    }

    private void markRoots() throws AllocationException {
        // mark all roots as grey
        try {
            for (EpiscopalObject root : roots)
                if (root.getGCNode().type() != NodeType.GREY)
                    makeGrey(root.getGCNode());
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    private void scan() throws AllocationException {
        try {
            if (scan == top)
                return;
            printTreadmill("before scan");
            scan = scan.prev.getInstance();
            for (ReferenceProperty reference : scan.data.getInstance().reachableReferences()) {
                // if the reference is null, don't follow it
                if (reference.getInstance() == null)
                    continue;
                // this is a node which scan's value points to
                GCNode<? super EpiscopalObject> referenceNode = ((EpiscopalObject)reference.getInstance()).getGCNode();
                // if the node of the pointed-to value is an ECRU node, make it grey
                if (referenceNode.type() == NodeType.ECRU) {
                    makeGrey(referenceNode);
                }
            }
            scan.setType(NodeType.BLACK);
            printTreadmill("after scan");
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    private void flip() throws AllocationException {
        try {
            printTreadmill("before flip");
            while (scan != top)
                scan();
            // turn all ecru nodes into white nodes (freeing their linked data)
            GCNode<? super EpiscopalObject> node = bottom;
            while (node != top) {
                heapAllocator.free(node.data.getInstance());
                node.setType(NodeType.WHITE);
                node = node.next.getInstance();
            }
            bottom = top;
            addNewFreeNode();
            printTreadmill("after adding new free node");
            // turn all black nodes into ecru
            node = scan;
            while (node != free) {
                makeEcru(node);
                node = node.next.getInstance();
            }
            markRoots();
            printTreadmill("after flip");
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    private void addNewFreeNode() throws PropertyAccessException, AllocationException {
        GCNode<? super EpiscopalObject> node = new GCNode<>(null);
        heapAllocator.allocate(node);
        nodes.add(node);
        node.setType(NodeType.WHITE);
        insertPrev(free, node);
        free = node;
    }

    public void printTreadmill(String state) {
        String message = "____________ " + state + " ____________";
        System.out.println(message);
        GCNode<? super EpiscopalObject> node = top;
        StringBuilder nodesString = new StringBuilder(treadmillNodeRepresentation(node));
        if (node.next.getInstance() != null && node.next.getInstance() != top)
            nodesString.append(" <==\\");
        String lastLine = String.copyValueOf(nodesString.toString().toCharArray());
        while (node.next.getInstance() != null && node.next.getInstance() != top) {
            String connector = "\n/" + repeatString("=", lastLine.length() - 2) + "/";
            String line = "\\==> " + treadmillNodeRepresentation(node.next.getInstance());
            if (node.next.getInstance() != null && node.next.getInstance().next.getInstance() != null && node.next.getInstance().next.getInstance() != top)
                line += " <==\\";
            nodesString.append(connector).append("\n").append(line);
            lastLine = line;
            node = node.next.getInstance();
        }
        nodesString.append(" <====> ").append(treadmillNodeLabel(top));
        System.out.println(nodesString.toString() + "\n");
    }

    private String repeatString(String s, int times) {
        StringBuilder footer = new StringBuilder();
        for (int i = 0; i < times; i++)
            footer.append(s);
        return footer.toString();
    }

    private String treadmillNodeRepresentation(GCNode<? super EpiscopalObject> node) {
        String output;
        String name = treadmillNodeLabel(node);
        try {
            MemoryManagedObject data = node.data.getInstance();
            output = "[" + name + ": " + node.type().name() + ": " + (data == null ? "NULL" : data.toString()) + "]";
        } catch (PropertyAccessException e) {
            output = "[" + name + ": ERROR: " + e.getMessage() + "]";
        }
        return output;
    }

    private String treadmillNodeLabel(GCNode<? super EpiscopalObject> node) {
        String name = "/";
        if (node == top)
            name += "T/";
        if (node == scan)
            name += "S/";
        if (node == free)
            name += "F/";
        if (node == bottom)
            name += "B/";
        return name;
    }
}
