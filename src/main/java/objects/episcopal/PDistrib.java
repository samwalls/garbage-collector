package objects.episcopal;

import objects.properties.IntProperty;
import objects.episcopal.representations.PDistributionRepresentation;

public class PDistrib<T extends PDistributionRepresentation> extends Distrib<T> {

    public final IntProperty nParams = new IntProperty();
    private final IntProperty[] paramProperties;

    public PDistrib(final Class<T> clazz, int nElements, int n) {
        super(clazz, nElements);
        paramProperties = new IntProperty[n];
        addProperty(nParams);
        for (int i = 0; i < n; i++) {
            paramProperties[i] = new IntProperty();
            addProperty(paramProperties[i]);
        }
    }

    public IntProperty paramAddress(int i) {
        return paramProperties[i];
    }
}
