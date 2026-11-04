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

    public boolean removeById(int id) {
        Node prev = null;
        for (Node c = head; c != null; prev = c, c = c.next) {
            if (c.pet.getId() == id) {
                if (prev == null) head = c.next; else prev.next = c.next;
                if (c == tail) tail = prev;
                size--;
                return true;
            }
        }
        return false;
    }
}