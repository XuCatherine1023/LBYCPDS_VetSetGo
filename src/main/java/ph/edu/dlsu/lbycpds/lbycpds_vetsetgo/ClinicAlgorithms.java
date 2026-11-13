package ph.edu.dlsu.lbycpds.lbycpds_vetsetgod;

import java.util.Collections;
import java.util.List;

public class ClinicAlgorithms {

    public static void quickSortAppointments(List<Appointment> list, int low, int high) {
        if (low < high) {
            int pi = partitionAppointments(list, low, high);
            quickSortAppointments(list, low, pi - 1);
            quickSortAppointments(list, pi + 1, high);
        }
    }

    private static int partitionAppointments(List<Appointment> list, int low, int high) {
        int pivot = list.get(high).getTimeSlot();
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (list.get(j).getTimeSlot() < pivot) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }

    public static int binarySearchTimeSlot(int[] sortedSlots, int targetTime) {
        int left = 0, right = sortedSlots.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (sortedSlots[mid] == targetTime) return mid;
            if (sortedSlots[mid] < targetTime) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }

    public static void quickSortPetsByName(List<Pet> pets, int low, int high) {
        if (low < high) {
            int pi = partitionPets(pets, low, high);
            quickSortPetsByName(pets, low, pi - 1);
            quickSortPetsByName(pets, pi + 1, high);
        }
    }

    private static int partitionPets(List<Pet> pets, int low, int high) {
        String pivot = pets.get(high).getName();
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (pets.get(j).getName().compareToIgnoreCase(pivot) <= 0) {
                i++;
                Collections.swap(pets, i, j);
            }
        }
        Collections.swap(pets, i + 1, high);
        return i + 1;
    }

    public static int binarySearchPetByName(List<Pet> sortedPets, String targetName) {
        int left = 0, right = sortedPets.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = sortedPets.get(mid).getName().compareToIgnoreCase(targetName);
            if (cmp == 0) return mid;
            if (cmp < 0) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }
}