package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

import java.util.*;

public class ClinicStructures {

    public static final String[] CLINIC_HOURS = {
            "Monday: 8AM-5PM", "Tuesday: 8AM-5PM", "Wednesday: 8AM-5PM",
            "Thursday: 8AM-5PM", "Friday: 8AM-5PM", "Saturday: 9AM-2PM", "Sunday: Closed"
    };

    public static class AppointmentQueue {
        private LinkedList<Appointment> queue = new LinkedList<>();
        public void enqueue(Appointment apt) { queue.addLast(apt); }
        public Appointment dequeue() { return queue.isEmpty() ? null : queue.removeFirst(); }
        public boolean isEmpty() { return queue.isEmpty(); }
    }

    public static class RecordStack {
        private LinkedList<Pet> stack = new LinkedList<>();
        public void push(Pet pet) { stack.addFirst(pet); }
        public Pet pop() { return stack.isEmpty() ? null : stack.removeFirst(); }
        public boolean isEmpty() { return stack.isEmpty(); }
    }

    public static class PriorityHeap {
        private ArrayList<Appointment> heap = new ArrayList<>();

        public void insert(Appointment apt) {
            heap.add(apt);
            siftUp(heap.size() - 1);
        }

        public Appointment extractMax() {
            if (heap.isEmpty()) return null;
            Appointment max = heap.get(0);
            Appointment last = heap.remove(heap.size() - 1);
            if (!heap.isEmpty()) {
                heap.set(0, last);
                siftDown(0);
            }
            return max;
        }

        private void siftUp(int index) {
            while (index > 0) {
                int parent = (index - 1) / 2;
                if (heap.get(index).compareTo(heap.get(parent)) > 0) {
                    Collections.swap(heap, index, parent);
                    index = parent;
                } else break;
            }
        }

        private void siftDown(int index) {
            int size = heap.size();
            while (index < size) {
                int left = 2 * index + 1;
                int right = 2 * index + 2;
                int largest = index;

                if (left < size && heap.get(left).compareTo(heap.get(largest)) > 0) largest = left;
                if (right < size && heap.get(right).compareTo(heap.get(largest)) > 0) largest = right;

                if (largest != index) {
                    Collections.swap(heap, index, largest);
                    index = largest;
                } else break;
            }
        }
    }
}