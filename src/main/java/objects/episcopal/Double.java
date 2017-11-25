package objects.episcopal;

import objects.DoubleProperty;

public class Double extends EpiscopalObject {

    private static final DoubleProperty valueProperty = new DoubleProperty();

    public Double(int address) {
        super(address);
        addProperty(valueProperty);
    }

    public java.lang.Double getValue() {
        return valueProperty.unmarshall(readForProperty(valueProperty));
    }

    public void setValue(java.lang.Double value) {
        writeForProperty(valueProperty, valueProperty.marshall(value));
    }
}
