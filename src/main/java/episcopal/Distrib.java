package episcopal;

import object.management.NullHeapException;
import object.properties.ClassProperty;
import object.properties.IntProperty;
import episcopal.representations.DistributionRepresentation;
import object.properties.ReferenceProperty;

import java.util.ArrayList;
import java.util.List;

public class Distrib<T extends DistributionRepresentation> extends EpiscopalObject {

    private final Class<T> distributionClass;
    public final ClassProperty<T> distributionType;
    public final IntProperty nElements = new IntProperty();
    private final List<ReferenceProperty<EpiscopalObject>> elementProperties;

    public Distrib(final Class<T> distributionClass, int n) {
        super();
        this.distributionClass = distributionClass;
        distributionType = new ClassProperty<>(distributionClass);
        elementProperties = new ArrayList<>();
        addProperty(distributionType);
        addProperty(nElements);
        for (int i = 0; i < n; i++) {
            ReferenceProperty<EpiscopalObject> p = new ReferenceProperty<>(null);
            elementProperties.add(p);
            addProperty(p);
        }
    }

    @Override
    public void onAllocate() throws NullHeapException {
        set(distributionType, distributionClass);
    }

    public ReferenceProperty<EpiscopalObject> elementAddress(int i) {
        return elementProperties.get(i);
    }
}
