public class MemoryArena {
    public final byte[] memory;
    private int offset = 0;

    private int NODE_SIZE = 8;
    private int VALUE_OFFSET = 0;
    private int NEXT_OFFSET = 4;

    public MemoryArena(int size) {
        memory = new byte[size];
    }

    public int alloc(int size) {
        if (offset + size > memory.length) {
            throw new RuntimeException("Out of memory!");
        }
        int start = offset;
        offset += size;
        return start;
    }

    public void reset() {
        offset = 0;
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
        memory[addr] = x;
    }

    public byte getByte(int addr) {
        return memory[addr];
    }

    //big endian approach
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

    //node creation & manip
    public int createNode(int val) {
        int nodeAddr = alloc(8);
        putInt(nodeAddr + VALUE_OFFSET, val);
        putInt(nodeAddr + NEXT_OFFSET, -1);

        return nodeAddr;
    }

    public int getValue(int nodeAddr) {
        return getInt(nodeAddr + VALUE_OFFSET);
    }

    public void setNext(int nodeAddr, int nextAddr) {
        checkAddr(nodeAddr, 4);
        checkNodePtr(nextAddr);
        putInt(nodeAddr + NEXT_OFFSET, nextAddr);
    }

    public int getNext(int nodeAddr) {
        return getInt(nodeAddr + NEXT_OFFSET);
    }

    public void printList(int headAddr) {
        if (headAddr == -1) {
            return;
        } else if (checkNodePtr(headAddr)) {
            System.out.print(getValue(headAddr) + " "); 
            while (getNext(headAddr) != -1 && checkNodePtr(getNext(headAddr))) {
                headAddr = getNext(headAddr);
                System.out.print(getValue(headAddr) + " ");
            }
        }
    }

    public boolean checkAddr(int addr, int bytesNeeded) {
        if (addr >= 0 && addr + bytesNeeded <= offset) return true;
        throw new RuntimeException("Not enough memory!");
    }

    public boolean checkNodePtr(int ptr) {
        if (ptr == -1) {
            return true;
        }
        if (ptr >= 0 && ptr + NODE_SIZE <= offset) {
            return true;
        }
        throw new RuntimeException("invalid pointer");
    }

    
}
