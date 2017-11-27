package gc;

public class Heap {

    public static final int NULL = -1;

    private long[] memory;

    public Heap(int heapSize) {
        memory = new long[heapSize];
    }

    public void put(int address, long[] data) {
        System.arraycopy(data, 0, memory, address, data.length);
    }

    public void put(int address, long data) {
        memory[address] = data;
    }

    public long[] get(int address, int length) {
        long[] data = new long[length];
        System.arraycopy(memory, address, data, 0, length);
        return data;
    }

    public long get(int address) {
        return memory[address];
    }

    public int getSize() {
        return memory.length;
    }
}
