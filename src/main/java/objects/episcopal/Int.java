package objects.episcopal;

import objects.properties.IntProperty;
import objects.NullHeapException;

public class Int extends EpiscopalObject {

    private static final IntProperty valueProperty = new IntProperty();

    public Int() {
        super();
        addProperty(valueProperty);
    }

    public int getValue() throws NullHeapException {
        return valueProperty.unmarshall(readForProperty(valueProperty));
    }

    public void setValue(int value) throws NullHeapException {
        writeForProperty(valueProperty, valueProperty.marshall(value));
    }
}
