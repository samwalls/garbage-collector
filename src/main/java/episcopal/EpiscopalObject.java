package episcopal;

import gc.GCNode;
import object.management.MemoryManagedObject;

public abstract class EpiscopalObject extends MemoryManagedObject {

    private GCNode<? super EpiscopalObject> gcNode;

    public EpiscopalObject() {
        super();
    }

    public GCNode<? super EpiscopalObject> getGCNode() {
        return gcNode;
    }

    public void setGCNode(GCNode<? super EpiscopalObject> gcNode) {
        this.gcNode = gcNode;
    }
}
