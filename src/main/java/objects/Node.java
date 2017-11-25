package objects;

import objects.episcopal.EpiscopalObject;

public class Node<T extends EpiscopalObject> extends MemoryManagedObject {

    private static final NodeType[] nodeTypes = NodeType.values();

    private static final IntProperty typeProperty = new IntProperty();
    private static final IntProperty prevProperty = new IntProperty();
    private static final IntProperty nextProperty = new IntProperty();
    private static final IntProperty dataAddressProperty = new IntProperty();

    public Node() {
        super();
        addProperty(typeProperty);
        addProperty(prevProperty);
        addProperty(nextProperty);
        addProperty(dataAddressProperty);
    }

    public int getDataAddress() throws NullHeapException {
        return dataAddressProperty.unmarshall(readForProperty(dataAddressProperty));
    }

    public void setDataAddress(int address) throws NullHeapException {
        writeForProperty(dataAddressProperty, dataAddressProperty.marshall(address));
    }

    public NodeType getType() throws NullHeapException {
        return nodeTypes[typeProperty.unmarshall(readForProperty(typeProperty))];
    }

    public Node getPrev() throws NullHeapException {
        Node n = new Node();
        n.setAddress(prevProperty.unmarshall(readForProperty(prevProperty)));
        return n;
    }

    public void setPrev(Node prev) {
        prevProperty.marshall(prev.getAddress());
    }

    public Node getNext() throws NullHeapException {
        Node n = new Node();
        n.setAddress(nextProperty.unmarshall(readForProperty(nextProperty)));
        return n;
    }

    public void setNext(Node prev) {
        nextProperty.marshall(prev.getAddress());
    }
}
