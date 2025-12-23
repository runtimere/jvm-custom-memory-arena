public class MemoryRegion {
    private final int startAddr;
    private final int endAddr;
    private final String name;

    public MemoryRegion(int startAddr, int endAddr, String name) {
        if (startAddr < 0 || endAddr < startAddr) {
            throw new IllegalArgumentException("Invalid region boundaries");
        }
        this.startAddr = startAddr;
        this.endAddr = endAddr;
        this.name = name != null ? name : "Unnamed";
    }

    public boolean contains(int addr) {
        return addr >= startAddr && addr < endAddr;
    }

    public boolean containsRange(int addr, int size) {
        return addr >= startAddr && (addr + size) <= endAddr;
    }

    public int getStartAddr() {
        return startAddr;
    }

    public int getEndAddr() {
        return endAddr;
    }

    public int getSize() {
        return endAddr - startAddr;
    }

    public String getName() {
        return name;
    }

    public boolean overlaps(MemoryRegion other) {
        return !(endAddr <= other.startAddr || startAddr >= other.endAddr);
    }

    @Override
    public String toString() {
        return String.format("%s: [0x%X - 0x%X] (%d bytes)", name, startAddr, endAddr, getSize());
    }
}

