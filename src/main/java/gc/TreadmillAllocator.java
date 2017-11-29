package gc;

import episcopal.EpiscopalObject;
import object.management.MemoryManagedObject;
import object.management.PropertyAccessException;
import object.properties.ReferenceProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static gc.NodeType.*;

/**
 * An implementation of <i>Henry G. Baker's</i> real-time treadmill garbage collector as an {@link Allocator} of
 * {@link EpiscopalObject} types.
 *
 * @see <a href="http://home.pipeline.com/~hbaker1/NoMotionGC.html">"The Treadmill: Real-Time Garbage Collection Without Motion Sickness"</a> by Henry G. Baker
 * @see <a href="http://www.memorymanagement.org/glossary/t.html#term-treadmill">memorymanagement.org/glossary/t.html#term-treadmill</a>
 */
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

    /**
     * @param heapSize the size of the heap to use
     * @param scanFrequency the number of allocations that must pass before a scan is forced
     * @param roots the set of root objects to discern from others
     * @param debugMode the debug mode to use (higher debug modes equate to more verbose output)
     * @throws AllocationException if there was a problem allocating any of the root objects
     */
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

    /**
     * @param heapSize the size of the heap to use
     * @param scanFrequency the number of allocations that must pass before a scan is forced
     * @param roots the set of root objects to discern from others
     * @throws AllocationException if there was a problem allocating any of the root objects
     */
    public TreadmillAllocator(int heapSize, int scanFrequency, Collection<EpiscopalObject> roots) throws AllocationException {
        this(heapSize, scanFrequency, roots, DebugMode.NONE);
    }

    /**
     * @param scanFrequency the number of allocations that must pass before a scan is forced
     * @param roots the set of root objects to discern from others
     * @param debugMode the debug mode to use (higher debug modes equate to more verbose output)
     * @throws AllocationException if there was a problem allocating any of the root objects
     */
    public TreadmillAllocator(int scanFrequency, Collection<EpiscopalObject> roots, DebugMode debugMode) throws AllocationException {
        this(BasicAllocator.HEAP_SIZE_DEFAULT, scanFrequency, roots, debugMode);
    }

    /**
     * @param roots the set of root objects to discern from others
     * @throws AllocationException if there was a problem allocating any of the root objects
     */
    public TreadmillAllocator(Collection<EpiscopalObject> roots) throws AllocationException {
        this(1, roots, DebugMode.NONE);
    }

    /**
     * @param roots the set of root objects to discern from others
     * @param debugMode the debug mode to use (higher debug modes equate to more verbose output)
     * @throws AllocationException if there was a problem allocating any of the root objects
     */
    public TreadmillAllocator(Collection<EpiscopalObject> roots, DebugMode debugMode) throws AllocationException {
        this(BasicAllocator.HEAP_SIZE_DEFAULT, 1, roots, debugMode);
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
        if (object == null)
            throw new AllocationException("cannot free null object");
        printTreadmill("before freeing object " + object.toString(), DebugMode.NORMAL);
        if (object.getGCNode() == null || object.getGCNode().getAddress() == Heap.NULL)
            throw new AllocationException("cannot free object " + object.toString() + " because it is not allocated on this heap");
        if (roots.contains(object))
            throw new AllocationException("cannot free a distinguished root object");
        try {
            GCNode<? super EpiscopalObject> node = object.getGCNode();
            // disassociate the object with it's GC node
            node.data.setInstance(null);
            object.setGCNode(null);
            // free the object's allocated space (disassociating it from the heap)
            heapAllocator.free(object);
            // make the node white, for later use
            make(node, WHITE);
        } catch (PropertyAccessException e) {
            throw new AllocationException("failed to disassociate the object " + object.toString() + " with the allocator's heap");
        }
        printTreadmill("after freeing object " + object.toString(), DebugMode.BASIC);
    }

    //******** TREADMILL HELPERS ********//

    /**
     * Scan a grey node, if any exist.
     * @throws AllocationException
     */
    private void scan() throws AllocationException {
        try {
            if (!anyOfType(GREY))
                return;
            GCNode<? super EpiscopalObject> toScan = getFront(GREY);
            printTreadmill("starting scan for " + treadmillNodeRepresentation(toScan), DebugMode.VERBOSE);
            // for each reachable object in the node to scan
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

    /**
     * Flip is called if there are no free nodes left to assign to an object.
     * All ecru nodes at present (objects which have been deemed unreachable) are made white, and their objects are
     * freed in the process. A new white node is also added to ensure that there are enough to associate with a new
     * object.
     * All black nodes are made ecru, making them "condemned" candidates for removal. If however they are later scanned
     * as root objects (made black), or deemed as reachable (made grey), it is ensured they won't be removed on the next
     * {@link TreadmillAllocator#flip} call.
     * @throws AllocationException if any of the properties of the objects being flipped could not be accessed, or there
     * was an issue freeing any unreachable objects.
     */
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

    /**
     * Put the treadmill in a state where normal operation can resume.
     * @param initialRoots the set of root objects to allocate initially, and discern from others
     * @throws AllocationException if there was a problem allocating any of the root objects
     * @throws PropertyAccessException if there was a problem accessing the new node's properties
     */
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

    /**
     * @param object the object to allocate as a root object
     * @return a {@link GCNode} which references the given object as its data
     * @throws AllocationException if a new {@link GCNode} could not be allocated
     * @throws PropertyAccessException if there was a problem setting any of the properties on the new node
     */
    private GCNode<? super EpiscopalObject> allocateRootObject(EpiscopalObject object) throws AllocationException, PropertyAccessException {
        GCNode<? super EpiscopalObject> node = new GCNode<>(object);
        object.setGCNode(node);
        heapAllocator.allocate(object);
        heapAllocator.allocate(node);
        node.setType(GREY);
        nodes.add(node);
        return node;
    }

    /**
     * Mark all roots as grey.
     * @throws AllocationException if there was a problem changing any properties of the nodes associated with root
     * objects
     */
    private void markRoots() throws AllocationException {
        try {
            for (EpiscopalObject root : roots)
                if (root.getGCNode().type() != GREY)
                    make(root.getGCNode(), GREY);
        } catch (PropertyAccessException e) {
            throw new AllocationException(e);
        }
    }

    /**
     * Allocate and add a new white node to the treadmill.
     * @throws PropertyAccessException if there was a problem setting any of the properties on the node
     * @throws AllocationException if there was a problem allocating the new node
     */
    private void addNewFreeNode() throws PropertyAccessException, AllocationException {
        GCNode<? super EpiscopalObject> node = new GCNode<>(null);
        heapAllocator.allocate(node);
        nodes.add(node);
        make(node, NodeType.WHITE);
    }

    /**
     * Given a colour, set the "front" for the colour to the given node. The "front" is the first node of the colour
     * type in the treadmill's doubly-linked list.
     * @param colour the colour to set the front for
     * @param node the node to set the front as
     */
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

    /**
     * Given a colour, get the node representing the "front" for the colour. The "front" is the first node of the colour
     * type in the treadmill's doubly-linked list.
     * @param colour the colour to get the front for
     * @return the node representing the front, if it exists, null otherwise
     */
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

    /**
     * Reassign the coloured fronts if necessary, given that the passed node was just inserted beside one.
     * @param node the node which has just been added
     * @throws PropertyAccessException
     */
    private void reassignPointers(GCNode<? super EpiscopalObject> node) throws PropertyAccessException {
        // scenario: the given node has just been inserted to the right of a treadmill pointer
        // only one element in the doubly-linked list
        if (node == node.next.getInstance())
            return;
        if (getFront(node.type()) == null || getFront(node.type()).type() != node.type())
            setFront(node.type(), node);
    }

    /**
     * @return true if there are any nodes of this colour type
     */
    private boolean anyOfType(NodeType type) {
        return getFront(type) != null;
    }

    /**
     * @param colour the colour of the node we want to insert into the treadmill
     * @return the node which the passed node should be inserted to the left of, in order to maintain some colour
     * invariant.
     */
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
                if (bottom != null)
                    return bottom;
                if (top != null)
                    return top;
            case WHITE:
                if (anyOfType(WHITE))
                    return getFront(WHITE).next.getInstance();
                if (bottom != null)
                    return bottom;
                if (scan != null)
                    return scan;
                if (top != null)
                    return top;
        }
        return top;
    }

    /**
     * Change the colour of the given node to match the passed colour, and insert it to the correct place in the
     * treadmill, respecting the already existing coloured "fronts". See {@link TreadmillAllocator#getFront} and
     * {@link TreadmillAllocator#setFront}.
     * @param node the node to change the colour of, and insert to the correct place
     * @param colour the colour to set the node to
     * @throws PropertyAccessException if there was a problem accessing the properties of the given node
     */
    private void make(GCNode<? super EpiscopalObject> node, NodeType colour) throws PropertyAccessException {
        insertPrev(insertionPoint(colour), node);
        node.setType(colour);
        reassignPointers(node);
    }

    //******** NODE HELPERS ********//

    /**
     * Insert newPrev to the left of node, unlinking it from it's current position. All colour fronts will be
     * reassigned as necessary given the insertion.
     * @param node the target node
     * @param newPrev the node to insert as node's new prev
     */
    private void insertPrev(GCNode<? super EpiscopalObject> node, GCNode<?super EpiscopalObject> newPrev) throws PropertyAccessException {
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

    /**
     * Unset any fronts which are associated with this node specifically. If any neighbouring nodes are of the same
     * colour, they will inherit the role of the front.
     * @param node the node to disassociate from the coloured fronts
     * @throws PropertyAccessException if there was a problem accessing the properties of the node
     */
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

    /**
     * Unlink the node from its place in the treadmill, updating the coloured fronts as necessary.
     * @param node the node to unlink
     * @throws PropertyAccessException if there was a problem accessing the properties of the given node or either of
     * its neighbours
     */
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

    /**
     * @return true if there are no free nodes left to use for a newly allocated object
     * @throws PropertyAccessException if there was a problem accessing the properties of the current white front
     */
    private boolean isHeapFull() throws PropertyAccessException {
        return getFront(WHITE) == null || getFront(WHITE).type() != NodeType.WHITE;
    }

    /**
     * Given the object, allocate it onto the heap and associate it with a free node.
     * @param object the object to allocate into the treadmill
     * @throws AllocationException if there was a problem allocating the object or there are no free nodes left
     */
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

    /**
     * Print a representation of the current treadmill to standard-out.
     * @param state a label to associate with the current state of the treadmill
     * @param mode the debug mode to use for this message (output only visible if the treadmill's debug mode is equal to
     *             or higher than this)
     */
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
