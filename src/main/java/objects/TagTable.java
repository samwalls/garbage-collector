package objects;

import objects.managed.MemoryManagedObject;

import java.util.List;

public class TagTable {

    private final List<Class<? extends MemoryManagedObject>> classes;

    public TagTable(List<Class<? extends MemoryManagedObject>> classes) {
        this.classes = classes;
    }

    public int tag(Class<? extends MemoryManagedObject> type) throws TagNotFoundException {
        int index = classes.indexOf(type);
        if (index < 0)
            throw new TagNotFoundException("tag mapping for type \"" + type.getCanonicalName() + "\" is not defined");
        return index;
    }

    public Class<? extends MemoryManagedObject> getClass(int tag) throws TagNotFoundException {
        if (tag >= classes.size() || tag < 0)
            throw new TagNotFoundException("tag signature \"" + tag + "\" goes outside range of tags available");
        return classes.get(tag);
    }
}
