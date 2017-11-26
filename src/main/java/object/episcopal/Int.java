package object.episcopal;

import object.properties.IntProperty;

public class Int extends EpiscopalObject {

    public final IntProperty value = new IntProperty();

    public Int() {
        super();
        addProperty(value);
    }
}
