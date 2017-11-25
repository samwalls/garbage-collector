package objects.episcopal;

import objects.ClassProperty;
import objects.IntProperty;
import objects.episcopal.representations.DistributionRepresentation;

public class Distrib extends EpiscopalObject {

    private final ClassProperty<DistributionRepresentation> distributionTypeProperty;
    private static final IntProperty nElementsProperty = new IntProperty();
    private final IntProperty[] elementProperties;

    public Distrib(int address, final Class<? extends DistributionRepresentation> clazz, int nElements) {
        super(address);
        distributionTypeProperty = new ClassProperty<>(clazz);
        elementProperties = new IntProperty[nElements];
        addProperty(distributionTypeProperty);
        addProperty(nElementsProperty);
        for (int i = 0; i < nElements; i++) {
            elementProperties[i] = new IntProperty();
            addProperty(elementProperties[i]);
        }
    }

    public Class<? extends DistributionRepresentation> getDistribType() {
        return distributionTypeProperty.unmarshall(readForProperty(distributionTypeProperty));
    }

    public Integer getNElements() {
        return nElementsProperty.unmarshall(readForProperty(nElementsProperty));
    }

    public Integer getElementAddress(int i) {
        return elementProperties[i].unmarshall(readForProperty(elementProperties[i]));
    }

    public void setElementAddress(int i, Integer address) {
        writeForProperty(elementProperties[i], elementProperties[i].marshall(address));
    }
}
