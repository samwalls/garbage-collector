package objects.episcopal;

import objects.properties.ClassProperty;
import objects.properties.IntProperty;
import objects.NullHeapException;
import objects.episcopal.representations.DistributionRepresentation;

public class Distrib<T extends DistributionRepresentation> extends EpiscopalObject {

    private final Class<T> distributionClass;
    private final ClassProperty<T> distributionTypeProperty;
    private static final IntProperty nElementsProperty = new IntProperty();
    private final IntProperty[] elementProperties;

    public Distrib(final Class<T> distributionClass, int nElements) {
        super();
        this.distributionClass = distributionClass;
        distributionTypeProperty = new ClassProperty<>(distributionClass);
        elementProperties = new IntProperty[nElements];
        addProperty(distributionTypeProperty);
        addProperty(nElementsProperty);
        for (int i = 0; i < nElements; i++) {
            elementProperties[i] = new IntProperty();
            addProperty(elementProperties[i]);
        }
    }

    @Override
    public void onAllocate() throws NullHeapException {
        // write the class specification to the heap
        writeForProperty(distributionTypeProperty, distributionTypeProperty.marshall(distributionClass));
    }

    public Class<? extends DistributionRepresentation> getDistribType() throws NullHeapException {
        return distributionTypeProperty.unmarshall(readForProperty(distributionTypeProperty));
    }

    public int getNElements() throws NullHeapException {
        return nElementsProperty.unmarshall(readForProperty(nElementsProperty));
    }

    public int getElementAddress(int i) throws NullHeapException {
        return elementProperties[i].unmarshall(readForProperty(elementProperties[i]));
    }

    public void setElementAddress(int i, int address) throws NullHeapException {
        writeForProperty(elementProperties[i], elementProperties[i].marshall(address));
    }
}
