import java.util.ArrayList;
import java.util.List;

public class PetLinkedList {
    private static class Node {
        Pet pet;
        Node next;
        Node(Pet pet) { this.pet = pet; }
    }

    private Node head, tail;
    private int size;
}