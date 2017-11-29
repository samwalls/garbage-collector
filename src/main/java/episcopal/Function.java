package episcopal;

import object.management.NullHeapException;
import object.properties.ClassProperty;
import object.properties.IntProperty;
import episcopal.representations.ClosureRepresentation;
import object.properties.ReferenceProperty;

import java.util.ArrayList;
import java.util.List;

public class Function<T extends ClosureRepresentation> extends EpiscopalObject {

    private final Class<T> closureClass;
    public final ClassProperty<T> closureType;
    public final IntProperty nParams = new IntProperty();
    private final List<ReferenceProperty<EpiscopalObject>> paramProperties;

    public Function(final Class<T> closureClass, int n) {
        super();
        this.closureClass = closureClass;
        closureType = new ClassProperty<>(closureClass);
        paramProperties = new ArrayList<>();
        addProperty(closureType);
        addProperty(nParams);
        for (int i = 0; i < n; i++) {
            ReferenceProperty<EpiscopalObject> p = new ReferenceProperty<>(null);
            paramProperties.add(p);
            addProperty(p);
        }
    }

    @Override
    public void onAllocate() throws NullHeapException {
        set(closureType, closureClass);
    }

    public ReferenceProperty<EpiscopalObject> paramAddress(int i) {
        return paramProperties.get(i);
    }
}
