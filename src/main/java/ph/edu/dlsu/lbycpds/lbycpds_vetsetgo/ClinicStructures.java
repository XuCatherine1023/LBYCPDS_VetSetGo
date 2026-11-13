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

    // 3. Stack for Last In, First Out History
    public static class RecordStack {
        private LinkedList<Pet> stack = new LinkedList<>();
        public void push(Pet pet) { stack.addFirst(pet); }
        public Pet pop() { return stack.isEmpty() ? null : stack.removeFirst(); }
        public boolean isEmpty() { return stack.isEmpty(); }
    }
}