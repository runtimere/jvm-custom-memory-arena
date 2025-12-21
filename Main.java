public class Main {
    public static void main(String[] args) {
        MemoryArena arena = new MemoryArena(128);

        int a = arena.alloc(4);
        int b = arena.alloc(4);
        int c = arena.alloc(4);

        System.out.println("address of a: " + a);
        System.out.println("address of b: " + b);
        System.out.println("address of c: " + c);

        arena.reset();

        int d = arena.alloc(4);
        System.out.println("address of d: " + d);
        System.out.println("total capacity: " + arena.capacity());
        System.out.println("used capacity: " + arena.used());
        System.out.println("remaining capacity: " + arena.remaining());

        int x = 1024;
        int next = (x >>> 24) & 0xFF;
        int next1 = (x >>> 16) & 0xFF;
        int next2 = (x >>> 8) & 0xFF;
        int next3 = (x >>> 0) & 0xFF;
        System.out.println(Integer.toHexString(x));
        System.out.println(next + " " + next1 + " " + next2 + " " + next3);
        
        //big endian byte manip
        arena.putInt(0, 0x12345678);
        System.out.println(Integer.toHexString(0x12345678)); 

        System.out.println(Integer.toHexString(arena.memory[0]));
        System.out.println(Integer.toHexString(arena.memory[1]));
        System.out.println(Integer.toHexString(arena.memory[2]));
        System.out.println(Integer.toHexString(arena.memory[3]));

        // big endian byte read
        System.out.println(Integer.toHexString(arena.getInt(0)));
        //node tests
        arena.reset();
        System.out.println();
        int node1 = arena.createNode(10);
        int node2 = arena.createNode(20);
        int node3 = arena.createNode(30);

        System.out.println(node1);
        System.out.println(node2);

        //form linked list
        arena.setNext(node1, node2);
        arena.setNext(node2, node3);

        //grab next value in linked list
        System.out.println(arena.getNext(node1) + " f");

        //print entire linked list
        arena.printList(node1);

    }
}
