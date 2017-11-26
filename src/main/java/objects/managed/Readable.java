package objects.managed;

public interface Readable<T> {

    T get() throws PropertyAccessException;
}
