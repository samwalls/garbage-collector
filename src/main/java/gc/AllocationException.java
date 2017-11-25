package gc;

public class AllocationException extends Exception {

    public AllocationException(String message) {
        super(message);
    }

    public AllocationException(Exception cause) {
        super(cause);
    }
}
