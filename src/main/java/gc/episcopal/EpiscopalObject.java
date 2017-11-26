package gc.episcopal;

import gc.GCNode;
import object.management.MemoryManagedObject;

public abstract class EpiscopalObject extends MemoryManagedObject {

    private GCNode<? extends EpiscopalObject> gcNode;

    public EpiscopalObject() {
        super();
    }

    public GCNode<? extends EpiscopalObject> getGCNode() {
        return gcNode;
    }

    public void setGCNode(GCNode<? extends EpiscopalObject> gcNode) {
        this.gcNode = gcNode;
    }
}
