package object;

public interface Marshallable<T> {

    long[] marshall(T object);

    T unmarshall(long[] data);
}
