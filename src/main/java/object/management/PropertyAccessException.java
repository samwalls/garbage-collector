package object.management;

public class PropertyAccessException extends Exception {

    public PropertyAccessException(Exception cause) {
        super(cause);
    }

    public PropertyAccessException(String message) {
        super(message);
    }

    public PropertyAccessException(String message, Exception cause) {
        super(message, cause);
    }
}
