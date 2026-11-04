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

    public int removeByOwner(String ownerUsername) {
        int removed = 0;
        while (head != null && head.pet.getOwnerUsername().equals(ownerUsername)) {
            head = head.next;
            size--;
            removed++;
        }
        if (head == null) {
            tail = null;
            return removed;
        }
        Node prev = head;
        for (Node c = head.next; c != null; c = c.next) {
            if (c.pet.getOwnerUsername().equals(ownerUsername)) {
                prev.next = c.next;
                if (c == tail) tail = prev;
                size--;
                removed++;
            } else {
                prev = c;
            }
        }
        return removed;
    }

    public List<Pet> findByOwner(String ownerUsername) {
        List<Pet> out = new ArrayList<>();
        for (Node c = head; c != null; c = c.next)
            if (c.pet.getOwnerUsername().equals(ownerUsername)) out.add(c.pet);
        return out;
    }

    public List<Pet> toList() {
        List<Pet> out = new ArrayList<>();
        for (Node c = head; c != null; c = c.next) out.add(c.pet);
        return out;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public int maxId() {
        int max = 0;
        for (Node c = head; c != null; c = c.next) max = Math.max(max, c.pet.getId());
        return max;
    }
}