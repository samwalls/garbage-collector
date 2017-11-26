package object.episcopal;

import object.properties.IntProperty;
import object.episcopal.representations.PDistributionRepresentation;
import object.properties.ReferenceProperty;

import java.util.ArrayList;
import java.util.List;

public class PDistrib<T extends PDistributionRepresentation> extends Distrib<T> {

    public final IntProperty nParams = new IntProperty();
    private final List<ReferenceProperty<EpiscopalObject>> paramProperties;

    public PDistrib(final Class<T> clazz, int nElements, int n) {
        super(clazz, nElements);
        paramProperties = new ArrayList<>();
        addProperty(nParams);
        for (int i = 0; i < n; i++) {
            ReferenceProperty<EpiscopalObject> p = new ReferenceProperty<>(null);
            paramProperties.add(p);
            addProperty(p);
        }
    }

    public ReferenceProperty<EpiscopalObject> paramAddress(int i) {
        return paramProperties.get(i);
    }
}
