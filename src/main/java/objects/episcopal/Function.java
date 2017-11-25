package objects.episcopal;

import objects.properties.ClassProperty;
import objects.properties.IntProperty;
import objects.NullHeapException;
import objects.episcopal.representations.ClosureRepresentation;

public class Function<T extends ClosureRepresentation> extends EpiscopalObject {

    private final Class<T> closureClass;
    private final ClassProperty<T> closureClassProperty;
    private static final IntProperty nParamsProperty = new IntProperty();
    private IntProperty[] paramProperties;

    public Function(final Class<T> closureClass, int nParams) {
        super();
        this.closureClass = closureClass;
        closureClassProperty = new ClassProperty<>(closureClass);
        paramProperties = new IntProperty[nParams];
        addProperty(closureClassProperty);
        addProperty(nParamsProperty);
        for (int i = 0; i < nParams; i++) {
            paramProperties[i] = new IntProperty();
            addProperty(paramProperties[i]);
        }
    }

    @Override
    public void onAllocate() throws NullHeapException {
        writeForProperty(closureClassProperty, closureClassProperty.marshall(closureClass));
    }

    public Class<? extends ClosureRepresentation> getClosure() throws NullHeapException {
        return closureClassProperty.unmarshall(readForProperty(closureClassProperty));
    }

    public int getNParams() throws NullHeapException {
        return nParamsProperty.unmarshall(readForProperty(nParamsProperty));
    }

    private void setNParams(int nParams) throws NullHeapException {
        writeForProperty(nParamsProperty, nParamsProperty.marshall(nParams));
    }

    public int getParamAddress(int param) throws NullHeapException {
        return paramProperties[param].unmarshall(readForProperty(paramProperties[param]));
    }

    public void setParamAddress(int param, int address) throws NullHeapException {
        writeForProperty(paramProperties[param], paramProperties[param].marshall(address));
    }
}
