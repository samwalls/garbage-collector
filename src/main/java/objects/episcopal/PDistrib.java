package objects.episcopal;

import objects.properties.IntProperty;
import objects.NullHeapException;
import objects.episcopal.representations.PDistributionRepresentation;

public class PDistrib<T extends PDistributionRepresentation> extends Distrib<T> {

    private static final IntProperty nParamsProperty = new IntProperty();
    private final IntProperty[] paramProperties;

    public PDistrib(final Class<T> clazz, int nElements, int nParams) {
        super(clazz, nElements);
        paramProperties = new IntProperty[nParams];
        addProperty(nParamsProperty);
        for (int i = 0; i < nParams; i++) {
            paramProperties[i] = new IntProperty();
            addProperty(paramProperties[i]);
        }
    }

    public int getNParams() throws NullHeapException {
        return nParamsProperty.unmarshall(readForProperty(nParamsProperty));
    }

    public int getParamAddress(int i) throws NullHeapException {
        return paramProperties[i].unmarshall(readForProperty(paramProperties[i]));
    }

    public void setParamAddress(int i, int address) throws NullHeapException {
        writeForProperty(paramProperties[i], paramProperties[i].marshall(address));
    }
}
