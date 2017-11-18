package gc;

public abstract class Cell<T> implements Sizeable {

    protected T data;

    public Cell(T data) {
        this.data = data;
    }
}
