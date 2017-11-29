package episcopal;

import object.properties.ReferenceProperty;

public class Indirect<T extends EpiscopalObject> extends EpiscopalObject {

    public ReferenceProperty<T> value;

    public Indirect(T instance) {
        super();
        value = new ReferenceProperty<>(instance);
        addProperty(value);
    }

    public Indirect() {
        this(null);
    }
}
