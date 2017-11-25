package objects.episcopal;

import objects.properties.IntProperty;
import objects.NullHeapException;

public class Bool extends EpiscopalObject {

    private static final IntProperty valueProperty = new IntProperty();

    public Bool() {
        super();
        addProperty(valueProperty);
    }

    public boolean getValue() throws NullHeapException {
        return valueProperty.unmarshall(readForProperty(valueProperty)) == 1;
    }

    public void setValue(boolean v) throws NullHeapException {
        writeForProperty(valueProperty, valueProperty.marshall(v ? 1 : 0));
    }
}
