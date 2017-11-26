package objects.managed;

public abstract class ReadOnlyProperty<T> extends GeneralProperty<T> implements Readable<T> {

    @Override
    public T get() throws PropertyAccessException {
        return getParent().get(this);
    }
}
