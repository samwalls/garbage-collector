package objects;

public class ObjectProperty<T extends MemoryManagedObject> extends Property<T> {

    MemoryManagedObject object;

    public ObjectProperty(MemoryManagedObject object) {
        this.object = object;
    }

    @Override
    public long[] marshall(T object) {
        return new long[0];
    }

    @Override
    public T unmarshall(long[] data) {
        return null;
    }

    @Override
    public int size() {
        return object.size();
    }
}
