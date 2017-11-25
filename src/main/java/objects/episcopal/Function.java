package objects.episcopal;

import objects.ClassProperty;
import objects.IntProperty;
import objects.episcopal.representations.ClosureRepresentation;

public class Function extends EpiscopalObject {

    private final ClassProperty<ClosureRepresentation> closureClassProperty;
    private static final IntProperty nParamsProperty = new IntProperty();
    private IntProperty[] paramProperties;

    public Function(int address, final Class<? extends ClosureRepresentation> clazz, int nParams) {
        super(address);
        closureClassProperty = new ClassProperty<>(clazz);
        paramProperties = new IntProperty[nParams];
        addProperty(closureClassProperty);
        addProperty(nParamsProperty);
        for (int i = 0; i < nParams; i++) {
            paramProperties[i] = new IntProperty();
            addProperty(paramProperties[i]);
        }
    }

    public Class<? extends ClosureRepresentation> getClosure() {
        return closureClassProperty.unmarshall(readForProperty(closureClassProperty));
    }

    public Integer getNParams() {
        return nParamsProperty.unmarshall(readForProperty(nParamsProperty));
    }

    private void setNParams(Integer nParams) {
        writeForProperty(nParamsProperty, nParamsProperty.marshall(nParams));
    }

    public Integer getParamAddress(int param) {
        return paramProperties[param].unmarshall(readForProperty(paramProperties[param]));
    }

    public void setParamAddress(int param, Integer address) {
        writeForProperty(paramProperties[param], paramProperties[param].marshall(address));
    }
}
