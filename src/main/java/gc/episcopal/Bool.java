package gc.episcopal;

import object.properties.BooleanProperty;

public class Bool extends EpiscopalObject {

    public final BooleanProperty value = new BooleanProperty();

    public Bool() {
        super();
        addProperty(value);
    }
}
