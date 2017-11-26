package objects.properties;

import objects.managed.Property;

public class BooleanProperty extends Property<Boolean> {
    @Override
    public long[] marshall(Boolean object) {
        return new long[] { object ? 1 : 0 };
    }

    @Override
    public Boolean unmarshall(long[] data) {
        return data[0] != 0;
    }

    @Override
    public int size() {
        return 1;
    }
}
