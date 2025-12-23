import java.util.ArrayList;
import java.util.List;

public class MemoryArena {
    public final byte[] memory;
    private int offset = 0;
    private int alignmentWaste = 0;
    private List<MemoryRegion> regions = new ArrayList<>();

    public MemoryArena(int size) {
        memory = new byte[size];
    }

    public int align(int addr, int alignment) {
        if (alignment <= 0) {
            return addr;
        }
        int remainder = addr % alignment;
        if (remainder == 0) {
            return addr;
        }
        return addr + (alignment - remainder);
    }

    public int alloc(int size) {
        if (offset + size > memory.length) {
            throw new OutOfMemoryException(size, remaining(), capacity(), offset);
        }
        int start = offset;
        offset += size;
        return start;
    }

    public int allocAligned(int size, int alignment) {
        int alignedOffset = align(offset, alignment);
        int waste = alignedOffset - offset;
        
        if (alignedOffset + size > memory.length) {
            throw new OutOfMemoryException(size, remaining(), capacity(), offset);
        }
        
        alignmentWaste += waste;
        offset = alignedOffset + size;
        return alignedOffset;
    }

    public void reset() {
        offset = 0;
        alignmentWaste = 0;
        regions.clear();
    }

    public int capacity() {
        return memory.length;
    }

    public int used() {
        return offset;
    }

    public int remaining() {
        return memory.length - offset;
    }

    public void putByte(int addr, byte x) {
        checkAddr(addr, 1);
        memory[addr] = x;
    }

    public byte getByte(int addr) {
        checkAddr(addr, 1);
        return memory[addr];
    }

    public void putInt(int addr, int x) {
        int[] bytes = {(x >>> 24) & 0xFF, (x >>> 16) & 0xFF, (x >>> 8) & 0xFF, (x >>> 0) & 0xFF};
        checkAddr(addr, 4);
        for (int i = 0; i < 4; i++) {
            memory[addr + i] = (byte) bytes[i];
        }
    }

    public int getInt(int addr) {
        checkAddr(addr, 4);
        int reconstruct = (memory[addr] & 0xFF) << 24 | (memory[addr + 1] & 0xFF) << 16 | (memory[addr + 2] & 0xFF) << 8 | (memory[addr + 3] & 0xFF);
        return reconstruct;
    }

    public void putLong(int addr, long x) {
        long[] bytes = {
            (x >>> 56) & 0xFF, (x >>> 48) & 0xFF, (x >>> 40) & 0xFF, (x >>> 32) & 0xFF,
            (x >>> 24) & 0xFF, (x >>> 16) & 0xFF, (x >>> 8) & 0xFF, (x >>> 0) & 0xFF
        };
        checkAddr(addr, 8);
        for (int i = 0; i < 8; i++) {
            memory[addr + i] = (byte) bytes[i];
        }
    }

    public long getLong(int addr) {
        checkAddr(addr, 8);
        long reconstruct = 
            ((long)(memory[addr] & 0xFF) << 56) |
            ((long)(memory[addr + 1] & 0xFF) << 48) |
            ((long)(memory[addr + 2] & 0xFF) << 40) |
            ((long)(memory[addr + 3] & 0xFF) << 32) |
            ((long)(memory[addr + 4] & 0xFF) << 24) |
            ((long)(memory[addr + 5] & 0xFF) << 16) |
            ((long)(memory[addr + 6] & 0xFF) << 8) |
            ((long)(memory[addr + 7] & 0xFF));
        return reconstruct;
    }

    public void putShort(int addr, short x) {
        byte[] bytes = {(byte)((x >>> 8) & 0xFF), (byte)((x >>> 0) & 0xFF)};
        checkAddr(addr, 2);
        for (int i = 0; i < 2; i++) {
            memory[addr + i] = (byte) bytes[i];
        }
    }

    public short getShort(int addr) {
        checkAddr(addr, 2);
        short reconstruct = (short)(((short)(memory[addr] & 0xFF) << 8) | (short)(memory[addr + 1] & 0xFF));
        return reconstruct;
    }

    public void putChar(int addr, char x) {
        byte[] bytes = {(byte)((x >>> 8) & 0xFF), (byte)((x >>> 0) & 0xFF)};
        checkAddr(addr, 2);
        for (int i = 0; i < 2; i++) {
            memory[addr + i] = (byte) bytes[i];
        }
    }

    public char getChar(int addr) {
        checkAddr(addr, 2);
        char reconstruct = (char)(((char)(memory[addr] & 0xFF) << 8) | (char)(memory[addr + 1] & 0xFF));
        return reconstruct;
    }

    public void putBoolean(int addr, boolean x) {
        checkAddr(addr, 1);
        memory[addr] = (byte) (x ? 1 : 0);
    }

    public boolean getBoolean(int addr) {
        checkAddr(addr, 1);
        boolean reconstruct = (memory[addr] & 0xFF) == 1;
        return reconstruct;
    }

    public boolean checkAddr(int addr, int bytesNeeded) {
        if (addr >= 0 && addr + bytesNeeded <= offset) {
            return true;
        }
        throw new InvalidAddressException(addr, bytesNeeded, offset, capacity());
    }

    public int getAlignmentWaste() {
        return alignmentWaste;
    }

    public void resetAlignmentWaste() {
        alignmentWaste = 0;
    }

    public MemoryRegion createRegion(int startAddr, int size, String name) {
        if (startAddr < 0 || startAddr + size > memory.length) {
            throw new InvalidAddressException(startAddr, size, offset, capacity());
        }
        
        MemoryRegion region = new MemoryRegion(startAddr, startAddr + size, name);
        
        for (MemoryRegion existing : regions) {
            if (region.overlaps(existing)) {
                throw new RuntimeException("Region overlaps with existing region: " + existing);
            }
        }
        
        regions.add(region);
        return region;
    }

    public MemoryRegion createRegionAtOffset(int size, String name) {
        int startAddr = offset;
        int endAddr = startAddr + size;
        
        if (endAddr > memory.length) {
            throw new OutOfMemoryException(size, remaining(), capacity(), offset);
        }
        
        MemoryRegion region = new MemoryRegion(startAddr, endAddr, name);
        regions.add(region);
        offset = endAddr;
        return region;
    }

    public MemoryRegion findRegion(int addr) {
        for (MemoryRegion region : regions) {
            if (region.contains(addr)) {
                return region;
            }
        }
        return null;
    }

    public MemoryRegion findRegionForRange(int addr, int size) {
        for (MemoryRegion region : regions) {
            if (region.containsRange(addr, size)) {
                return region;
            }
        }
        return null;
    }

    public List<MemoryRegion> getAllRegions() {
        return new ArrayList<>(regions);
    }

    public void clearRegions() {
        regions.clear();
    }

    public boolean validateAddressInRegion(int addr, String regionName) {
        MemoryRegion region = findRegion(addr);
        if (region == null) {
            return false;
        }
        return regionName == null || region.getName().equals(regionName);
    }
}
