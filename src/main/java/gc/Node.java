package gc;

import object.episcopal.EpiscopalObject;
import object.management.MemoryManagedObject;
import object.management.PropertyAccessException;
import object.properties.IntProperty;
import object.properties.ReferenceProperty;

public class Node<T extends EpiscopalObject> extends MemoryManagedObject {

    private static final NodeType[] nodeTypes = NodeType.values();

    private final IntProperty type = new IntProperty();
    public final ReferenceProperty<Node<? super T>> prev;
    public final ReferenceProperty<Node<? super T>> next;
    public final ReferenceProperty<T> data;

    public Node(Node<? super T> prevInstance, Node<? super T> nextInstance, T dataInstance) {
        super();
        prev = new ReferenceProperty<>(prevInstance);
        next = new ReferenceProperty<>(nextInstance);
        data = new ReferenceProperty<>(dataInstance);
        addProperty(type);
        addProperty(prev);
        addProperty(next);
        addProperty(data);
    }

    public Node(T dataInstance) {
        this(null, null, dataInstance);
    }

    public NodeType type() throws PropertyAccessException {
        return nodeTypes[type.get()];
    }

    public void setType(NodeType nodeType) throws PropertyAccessException {
        type.set(nodeType.ordinal());
    }
}
