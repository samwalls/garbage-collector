package gc;

public class OutOfMemoryException extends AllocationException {

    public OutOfMemoryException(String message) {
        super(message);
    }
}
