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

    public void add(Pet pet) {
        Node n = new Node(pet);
        if (head == null) { head = tail = n; }
        else { tail.next = n; tail = n; }
        size++;
    }

    public Pet findById(int id) {
        for (Node c = head; c != null; c = c.next)
            if (c.pet.getId() == id) return c.pet;
        return null;
    }
}