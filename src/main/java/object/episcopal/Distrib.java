package object.episcopal;

import object.management.NullHeapException;
import object.properties.ClassProperty;
import object.properties.IntProperty;
import object.episcopal.representations.DistributionRepresentation;

public class Distrib<T extends DistributionRepresentation> extends EpiscopalObject {

    private final Class<T> distributionClass;
    public final ClassProperty<T> distributionType;
    public final IntProperty nElements = new IntProperty();
    private final IntProperty[] elementProperties;

    public Distrib(final Class<T> distributionClass, int n) {
        super();
        this.distributionClass = distributionClass;
        distributionType = new ClassProperty<>(distributionClass);
        elementProperties = new IntProperty[n];
        addProperty(distributionType);
        addProperty(nElements);
        for (int i = 0; i < n; i++) {
            elementProperties[i] = new IntProperty();
            addProperty(elementProperties[i]);
        }
    }

    @Override
    public void onAllocate() throws NullHeapException {
        set(distributionType, distributionClass);
    }

    public IntProperty elementAddress(int i) {
        return elementProperties[i];
    }
}
