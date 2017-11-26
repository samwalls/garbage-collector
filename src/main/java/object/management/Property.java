package object.management;

public abstract class Property<T> extends GeneralProperty<T> implements Readable<T>, Writable<T> {

    public T get() throws NullHeapException {
        return getParent().get(this);
    }

    public void set(T value) throws NullHeapException {
        getParent().set(this, value);
    }
}
