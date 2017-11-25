package gc;

public class FreeRegion {

    private int address, size;

    private FreeRegion next;

    public FreeRegion(int address, int size, FreeRegion next) {
        this.address = address;
        this.size = size;
        this.next = next;
    }

    public FreeRegion(int address, int size) {
        this(address, size, null);
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public FreeRegion getNext() {
        return next;
    }

    public void setNext(FreeRegion next) {
        this.next = next;
    }
}
