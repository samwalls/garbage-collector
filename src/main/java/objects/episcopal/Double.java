package objects.episcopal;

import objects.DoubleProperty;
import objects.NullHeapException;

public class Double extends EpiscopalObject {

    private static final DoubleProperty valueProperty = new DoubleProperty();

    public Double() {
        super();
        addProperty(valueProperty);
    }

    public double getValue() throws NullHeapException {
        return valueProperty.unmarshall(readForProperty(valueProperty));
    }

    public void setValue(double value) throws NullHeapException {
        writeForProperty(valueProperty, valueProperty.marshall(value));
    }
}
