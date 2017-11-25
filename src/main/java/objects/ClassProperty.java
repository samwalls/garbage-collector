package objects;

import org.apache.commons.lang3.SerializationUtils;

public class ClassProperty<T> extends Property<Class<? extends T>> {

    private final Class<? extends T> clazz;

    // cache this as the class definition will never change at runtime (we'd only hope)
    private long[] cache = null;

    /**
     * @param clazz The class to use as a closure representation, cannot be changed after the fact as the size must
     *              remain constant. If one requires the class value to change then one will have to create a new
     *              {@link MemoryManagedObject} somewhere else containing the new class property.
     */
    public ClassProperty(final Class<? extends T> clazz) {
        this.clazz = clazz;
        defaultCache();
    }

    private void defaultCache() {
        if (cache == null)
            marshall(clazz);
    }

    @Override
    public long[] marshall(Class<? extends T> object) {
        byte[] data = SerializationUtils.serialize(object);
        cache = new long[data.length];
        for (int i = 0; i < data.length; i++)
            cache[i] = data[i];
        return cache;
    }

    @Override
    public Class<? extends T> unmarshall(long[] data) {
        cache = data;
        byte[] buffer = new byte[cache.length];
        for (int i = 0; i < cache.length; i++)
            buffer[i] = (byte)cache[i];
        return SerializationUtils.deserialize(buffer);
    }

    @Override
    public int size() {
        return cache.length;
    }
}
