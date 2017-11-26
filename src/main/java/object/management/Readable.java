package object.management;

public interface Readable<T> {

    T get() throws PropertyAccessException;
}
