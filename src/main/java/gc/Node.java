package gc;

import object.episcopal.EpiscopalObject;
import object.management.MemoryManagedObject;
import object.management.PropertyAccessException;
import object.properties.IntProperty;

public class Node<T extends EpiscopalObject> extends MemoryManagedObject {

    private static final NodeType[] nodeTypes = NodeType.values();

    // keep an instance of the T type so that we can use it from a list of nodes
    private T instance;

    private final IntProperty type = new IntProperty();
    public final IntProperty prev = new IntProperty();
    public final IntProperty next = new IntProperty();
    public final IntProperty dataAddress = new IntProperty();

    public Node(T instance) {
        super();
        this.instance = instance;
        addProperty(type);
        addProperty(prev);
        addProperty(next);
        addProperty(dataAddress);
    }

    public T getInstance() {
        return instance;
    }

    public NodeType type() throws PropertyAccessException {
        return nodeTypes[type.get()];
    }

    public void setType(NodeType nodeType) throws PropertyAccessException {
        type.set(nodeType.ordinal());
    }
}
