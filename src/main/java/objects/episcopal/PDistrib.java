package objects.episcopal;

import objects.IntProperty;
import objects.episcopal.representations.PDistributionRepresentation;

public class PDistrib extends Distrib {

    private static final IntProperty nParamsProperty = new IntProperty();
    private final IntProperty[] paramProperties;

    public PDistrib(int address, final Class<? extends PDistributionRepresentation> clazz, int nElements, int nParams) {
        super(address, clazz, nElements);
        paramProperties = new IntProperty[nParams];
        addProperty(nParamsProperty);
        for (int i = 0; i < nParams; i++) {
            paramProperties[i] = new IntProperty();
            addProperty(paramProperties[i]);
        }
    }

    public Integer getNParams() {
        return nParamsProperty.unmarshall(readForProperty(nParamsProperty));
    }

    public Integer getParamAddress(int i) {
        return paramProperties[i].unmarshall(readForProperty(paramProperties[i]));
    }

    public void setParamAddress(int i, Integer address) {
        writeForProperty(paramProperties[i], paramProperties[i].marshall(address));
    }
}
