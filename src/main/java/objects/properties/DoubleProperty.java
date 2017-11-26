package objects.properties;

import objects.managed.Property;

public class DoubleProperty extends Property<Double> {

    @Override
    public long[] marshall(Double object) {
        return new long[] { Double.doubleToLongBits(object) };
    }

    @Override
    public Double unmarshall(long[] data) {
        return Double.longBitsToDouble(data[0]);
    }

    @Override
    public int size() {
        return 1;
    }
}
