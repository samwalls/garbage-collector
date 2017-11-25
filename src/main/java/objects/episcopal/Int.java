package objects.episcopal;

import objects.IntProperty;

public class Int extends EpiscopalObject {

    private static final IntProperty valueProperty = new IntProperty();

    public Int(int address) {
        super(address);
        addProperty(valueProperty);
    }

    public Integer getValue() {
        return valueProperty.unmarshall(readForProperty(valueProperty));
    }

    public void setValue(Integer value) {
        writeForProperty(valueProperty, valueProperty.marshall(value));
    }
}
