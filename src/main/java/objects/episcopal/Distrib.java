package objects.episcopal;

import objects.properties.ClassProperty;
import objects.properties.IntProperty;
import objects.NullHeapException;
import objects.episcopal.representations.DistributionRepresentation;

public class Distrib extends EpiscopalObject {

    private final ClassProperty<DistributionRepresentation> distributionTypeProperty;
    private static final IntProperty nElementsProperty = new IntProperty();
    private final IntProperty[] elementProperties;

    public Distrib(final Class<? extends DistributionRepresentation> clazz, int nElements) {
        super();
        distributionTypeProperty = new ClassProperty<>(clazz);
        elementProperties = new IntProperty[nElements];
        addProperty(distributionTypeProperty);
        addProperty(nElementsProperty);
        for (int i = 0; i < nElements; i++) {
            elementProperties[i] = new IntProperty();
            addProperty(elementProperties[i]);
        }
    }

    public Class<? extends DistributionRepresentation> getDistribType() throws NullHeapException {
        return distributionTypeProperty.unmarshall(readForProperty(distributionTypeProperty));
    }

    public Integer getNElements() throws NullHeapException {
        return nElementsProperty.unmarshall(readForProperty(nElementsProperty));
    }

    public Integer getElementAddress(int i) throws NullHeapException {
        return elementProperties[i].unmarshall(readForProperty(elementProperties[i]));
    }

    public void setElementAddress(int i, Integer address) throws NullHeapException {
        writeForProperty(elementProperties[i], elementProperties[i].marshall(address));
    }
}
