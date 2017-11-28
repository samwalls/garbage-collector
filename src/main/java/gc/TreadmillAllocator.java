package gc;

import gc.episcopal.EpiscopalObject;
import object.management.MemoryManagedObject;
import object.management.PropertyAccessException;
import object.properties.ReferenceProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static gc.NodeType.*;

public class TreadmillAllocator implements Allocator<EpiscopalObject> {

    private final int scanFrequency;
    private int currentScan = 0;

    private Set<GCNode<? super EpiscopalObject>> nodes;

    private Set<EpiscopalObject> roots;

    private BasicAllocator heapAllocator;

    private DebugMode debugMode;

    // top <-> ... grey nodes ... <-> scan
    // scan <-> ... black nodes ... <-> free
    // free <-> ... white nodes ... <-> bottom
    // bottom <-> ... ecru (off-white) nodes ... <-> top
    private GCNode<? super EpiscopalObject> top = null, scan = null, free = null, bottom = null;

    public TreadmillAllocator(int heapSize, int scanFrequency, Collection<EpiscopalObject> roots, DebugMode debugMode) throws AllocationException {
        if (scanFrequency <= 0)
            throw new IllegalArgumentException("illegal scan frequency: " + scanFrequency + "; frequency must be > 0");
        this.scanFrequency = scanFrequency;
        this.debugMode = debugMode;
        heapAllocator = new BasicAllocator(heapSize);
        try {
            initTreadmill(roots);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    public TreadmillAllocator(int heapSize, int scanFrequency, Collection<EpiscopalObject> roots) throws AllocationException {
        this(heapSize, scanFrequency, roots, DebugMode.NONE);
    }

    public TreadmillAllocator(int scanFrequency, Collection<EpiscopalObject> roots, DebugMode debugMode) throws AllocationException {
        this(BasicAllocator.HEAP_SIZE_DEFAULT, scanFrequency, roots, debugMode);
    }

    public TreadmillAllocator(int scanFrequency, Collection<EpiscopalObject> roots) throws AllocationException {
        this(BasicAllocator.HEAP_SIZE_DEFAULT, scanFrequency, roots);
    }

    //******** ALLOCATOR IMPLEMENTATION ********//

    @Override
    public void allocate(EpiscopalObject object) throws AllocationException {
        printTreadmill("before allocation", DebugMode.VERBOSE);
        // scan if necessary
        if (++currentScan >= scanFrequency) {
            scan();
            currentScan = 0;
        }
        // flip if there are no more free slots
        try {
            if (isHeapFull()) {
                flip();
                if (isHeapFull())
                    throw new OutOfMemoryException("heap is full");
            }
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
        allocateObjectIntoFree(object);
        printTreadmill("after allocation of " + object.toString(), DebugMode.BASIC);
    }

    @Override
    public void free(EpiscopalObject object) throws AllocationException {
        GCNode<? super EpiscopalObject> node = object.getGCNode();
        // TODO
    }

    //******** TREADMILL HELPERS ********//

    private void scan() throws AllocationException {
        try {
            if (!anyOfType(GREY))
                return;
            GCNode<? super EpiscopalObject> toScan = getFront(GREY);
            printTreadmill("starting scan for " + treadmillNodeRepresentation(toScan), DebugMode.VERBOSE);
            for (ReferenceProperty reference : toScan.data.getInstance().reachableReferences()) {
                // if the reference is null, don't follow it
                if (reference.getInstance() == null)
                    continue;
                // this is a node which scan's value points to
                GCNode<? super EpiscopalObject> referenceNode = ((EpiscopalObject)reference.getInstance()).getGCNode();
                // if the node of the pointed-to value is an ECRU node, make it grey
                if (referenceNode.type() == NodeType.ECRU) {
                    make(referenceNode, GREY);
                }
            }
            make(toScan, BLACK);
            printTreadmill("finished scan for " + treadmillNodeRepresentation(toScan), DebugMode.NORMAL);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    private void flip() throws AllocationException {
        try {
            printTreadmill("starting flip", DebugMode.VERBOSE);
            while (top != null && scan != null)
                scan();
            // turn all ecru nodes into white nodes (freeing their linked data)
            GCNode<? super EpiscopalObject> node = getFront(ECRU);
            while (node != null && node.type() == NodeType.ECRU) {
                heapAllocator.free(node.data.getInstance());
                make(node, NodeType.WHITE);
                node = node.next.getInstance();
            }
            printTreadmill("turn ecru into white", DebugMode.VERBOSE);
            addNewFreeNode();
            printTreadmill("add new free node", DebugMode.VERBOSE);
            // turn all black nodes into ecru
            node = getFront(BLACK);
            while (node != null && node.type() == BLACK) {
                GCNode<? super EpiscopalObject> next = node.next.getInstance();
                String debugMessage = "moving node " + treadmillNodeRepresentation(node) + " to ECRU";
                make(node, NodeType.ECRU);
                printTreadmill(debugMessage, DebugMode.VERBOSE);
                node = next;
            }
            printTreadmill("turn black into ecru", DebugMode.VERBOSE);
            markRoots();
            printTreadmill("mark roots as grey / finish flip", DebugMode.NORMAL);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

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
        if (firstRoot != null)
            setFront(firstRoot.type(), firstRoot);
    }

    private GCNode<? super EpiscopalObject> allocateRootObject(EpiscopalObject object) throws AllocationException, PropertyAccessException {
        GCNode<? super EpiscopalObject> node = new GCNode<>(object);
        object.setGCNode(node);
        heapAllocator.allocate(object);
        heapAllocator.allocate(node);
        node.setType(GREY);
        nodes.add(node);
        return node;
    }

    private void markRoots() throws AllocationException {
        // mark all roots as grey
        try {
            for (EpiscopalObject root : roots)
                if (root.getGCNode().type() != GREY)
                    make(root.getGCNode(), GREY);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    private void addNewFreeNode() throws PropertyAccessException, AllocationException {
        GCNode<? super EpiscopalObject> node = new GCNode<>(null);
        heapAllocator.allocate(node);
        nodes.add(node);
        make(node, NodeType.WHITE);
    }

    private void setFront(NodeType colour, GCNode<? super EpiscopalObject> node) {
        switch (colour) {
            case GREY:
                top = node;
                return;
            case BLACK:
                scan = node;
                return;
            case WHITE:
                free = node;
                return;
            case ECRU:
                bottom = node;
                return;
        }
    }

    private GCNode<? super EpiscopalObject> getFront(NodeType colour) {
        switch (colour) {
            case GREY:
                return top;
            case BLACK:
                return scan;
            case WHITE:
                return free;
            case ECRU:
                return bottom;
        }
        return null;
    }

    private void reassignPointers(GCNode<? super EpiscopalObject> node) throws PropertyAccessException {
        // scenario: the given node has just been inserted to the right of a treadmill pointer
        // only one element in the doubly-linked list
        if (node == node.next.getInstance())
            return;
        if (getFront(node.type()) == null || getFront(node.type()).type() != node.type())
            setFront(node.type(), node);
    }

    /**
     * @return true if there are any nodes of this type
     */
    private boolean anyOfType(NodeType type) {
        return getFront(type) != null;
    }

    private GCNode<? super EpiscopalObject> insertionPoint(NodeType colour) {
        switch (colour) {
            case ECRU:
                if (anyOfType(ECRU))
                    return getFront(ECRU).next.getInstance();
                if (anyOfType(GREY))
                    return getFront(GREY);
                if (anyOfType(BLACK))
                    return getFront(BLACK);
                if (anyOfType(WHITE))
                    return getFront(WHITE);
            case GREY:
                if (anyOfType(GREY))
                    return getFront(GREY).next.getInstance();
                if (anyOfType(BLACK))
                    return getFront(BLACK);
                if (anyOfType(WHITE))
                    return getFront(WHITE);
                if (bottom != null)
                    return bottom;
            case BLACK:
                if (anyOfType(BLACK))
                    return getFront(BLACK).next.getInstance();
                if (anyOfType(WHITE))
                    return getFront((WHITE));
                if (top != null)
                    return top;
                if (bottom != null)
                    return bottom;
            case WHITE:
                if (anyOfType(WHITE))
                    return getFront(WHITE).next.getInstance();
                if (top != null)
                    return top;
                if (scan != null)
                    return scan;
                if (bottom != null)
                    return bottom;
        }
        return top;
    }

    private void make(GCNode<? super EpiscopalObject> node, NodeType colour) throws PropertyAccessException {
        insertPrevPreservePointers(insertionPoint(colour), node);
        node.setType(colour);
        reassignPointers(node);
    }

    //******** NODE HELPERS ********//

    /**
     * Insert prev to the left of node, unlinking it from it's current position.
     * @param node the target node
     * @param newPrev the node to insert as node's prev
     */
    private void insertPrevPreservePointers(GCNode<? super EpiscopalObject> node, GCNode<?super EpiscopalObject> newPrev) throws PropertyAccessException {
        if (node == newPrev) {
            unsetFronts(newPrev);
            return;
        }
        unlink(newPrev);
        GCNode<? super EpiscopalObject> lastPrev = node.prev.getInstance();
        // make surrounding nodes point to the inserted one
        lastPrev.next.setInstance(newPrev);
        node.prev.setInstance(newPrev);
        // make the inserted node point to the surrounding nodes
        newPrev.prev.setInstance(lastPrev);
        newPrev.next.setInstance(node);
    }

    private void unsetFronts(GCNode<? super EpiscopalObject> node) throws PropertyAccessException {
        // remove the front if it is associated with this
        if (getFront(node.type()) == node) {
            setFront(node.type(), null);
        }
        // set the front if the neighbour matches it
        if (getFront(node.type()) == null) {
            if (node.prev.getInstance() != null && node.prev.getInstance().type() == node.type()) {
                setFront(node.prev.getInstance().type(), node.prev.getInstance());
            } else if (node.next.getInstance() != null && node.next.getInstance().type() == node.type()) {
                setFront(node.next.getInstance().type(), node.next.getInstance());
            }
        }
    }

    private void unlink(GCNode<? super EpiscopalObject> node) throws PropertyAccessException {
        unsetFronts(node);
        if (node.next.getInstance() == node || node.prev.getInstance() == node) {
            return;
        }
        GCNode<? super EpiscopalObject> prev = node.prev.getInstance();
        GCNode<? super EpiscopalObject> next = node.next.getInstance();
        node.next.setInstance(null);
        node.prev.setInstance(null);
        if (prev != null)
            prev.next.setInstance(next);
        if (next != null)
            next.prev.setInstance(prev);
    }

    private boolean isHeapFull() throws PropertyAccessException {
        return free == null || free.type() != NodeType.WHITE;
    }

    private void allocateObjectIntoFree(EpiscopalObject object) throws AllocationException {
        try {
            heapAllocator.allocate(object);
            GCNode<? super EpiscopalObject> freeNode = getFront(WHITE);
            if (freeNode == null)
                throw new AllocationException("could not find free node to allocate object " + object.toString() + " with");
            object.setGCNode(freeNode);
            freeNode.data.setInstance(object);
            make(freeNode, BLACK);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    //******** DEBUGGING HELPERS ********//

    public void printTreadmill(String state, DebugMode mode) {
        if (debugMode == DebugMode.NONE || mode.ordinal() > debugMode.ordinal())
            return;
        String message = "____________ " + state + " ____________";
        System.out.println(message);
        GCNode<? super EpiscopalObject> front = firstAvailableFront();
        if (front == null) {
            System.out.println("no nodes");
            return;
        }
        GCNode<? super EpiscopalObject> node = front;
        StringBuilder nodesString = new StringBuilder(treadmillNodeRepresentation(node));
        if (node.next.getInstance() != null && node.next.getInstance() != front)
            nodesString.append(" <==\\");
        String lastLine = String.copyValueOf(nodesString.toString().toCharArray());
        while (node.next.getInstance() != null && node.next.getInstance() != front) {
            String connector = "\n/" + repeatString("=", lastLine.length() - 2) + "/";
            String line = "\\==> " + treadmillNodeRepresentation(node.next.getInstance());
            if (node.next.getInstance() != null && node.next.getInstance().next.getInstance() != null && node.next.getInstance().next.getInstance() != front)
                line += " <==\\";
            nodesString.append(connector).append("\n").append(line);
            lastLine = line;
            node = node.next.getInstance();
        }
        nodesString.append(" <====> ").append(treadmillNodeLabel(front));
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

    private GCNode<? super EpiscopalObject> firstAvailableFront() {
        if (bottom != null)
            return bottom;
        if (top != null)
            return top;
        if (scan != null)
            return scan;
        if (free != null)
            return free;
        return null;
    }
}
