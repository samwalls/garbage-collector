package objects.episcopal;

import objects.properties.IntProperty;

public class Int extends EpiscopalObject {

    public final IntProperty value = new IntProperty();

    public Int() {
        super();
        addProperty(value);
    }
}
