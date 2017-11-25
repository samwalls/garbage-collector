package objects.episcopal;

import objects.IntProperty;

public class Bool extends EpiscopalObject {

    private static final IntProperty valueProperty = new IntProperty();

    public Bool(int address) {
        super(address);
        addProperty(valueProperty);
    }

    public Boolean getValue() {
        Integer v = valueProperty.unmarshall(readForProperty(valueProperty));
        return v == 1;
    }

    public void setValue(Boolean v) {
        writeForProperty(valueProperty, valueProperty.marshall(v ? 1 : 0));
    }
}
