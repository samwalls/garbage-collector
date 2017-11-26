package object.management;

public interface Writable<T> {

    void set(T value) throws PropertyAccessException;
}
