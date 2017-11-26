package objects.managed;

public interface Writable<T> {

    void set(T value) throws PropertyAccessException;
}
