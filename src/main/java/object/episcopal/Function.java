package object.episcopal;

import object.management.NullHeapException;
import object.properties.ClassProperty;
import object.properties.IntProperty;
import object.episcopal.representations.ClosureRepresentation;

public class Function<T extends ClosureRepresentation> extends EpiscopalObject {

    private final Class<T> closureClass;
    public final ClassProperty<T> closureType;
    public final IntProperty nParams = new IntProperty();
    private final IntProperty[] paramProperties;

    public Function(final Class<T> closureClass, int n) {
        super();
        this.closureClass = closureClass;
        closureType = new ClassProperty<>(closureClass);
        paramProperties = new IntProperty[n];
        addProperty(closureType);
        addProperty(nParams);
        for (int i = 0; i < n; i++) {
            paramProperties[i] = new IntProperty();
            addProperty(paramProperties[i]);
        }
    }

    @Override
    public void onAllocate() throws NullHeapException {
        set(closureType, closureClass);
    }

    public IntProperty paramAddress(int i) {
        return paramProperties[i];
    }
}
