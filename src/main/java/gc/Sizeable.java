package gc;

/**
 * Objects which implement Sizeable provide a size in GC words.
 */
public interface Sizeable {

    /**
     * @return the size (in GC words) of this object
     */
    long size();
}
