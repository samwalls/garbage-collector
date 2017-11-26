package object.properties;

import object.management.Property;

public class IntProperty extends Property<Integer> {

    @Override
    public long[] marshall(Integer object) {
        return new long[] { object };
    }

    @Override
    public Integer unmarshall(long[] data) {
        return (int)data[0];
    }

    @Override
    public int size() {
        return 1;
    }
}
