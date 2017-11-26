package gc.episcopal;

import object.properties.DoubleProperty;

public class Double extends EpiscopalObject {

    public final DoubleProperty value = new DoubleProperty();

    public Double() {
        super();
        addProperty(value);
    }
}
