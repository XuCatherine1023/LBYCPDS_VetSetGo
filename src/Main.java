import java.io.Console;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String VET_ACCESS_CODE = "VETSETGO2026";

    private static final Scanner in = new Scanner(System.in);

    private static final FileStorage storage = new FileStorage("data");
    private static final AuthService auth = new AuthService();
    private static PetLinkedList pets = new PetLinkedList();
    private static int nextPetId = 1;

    public static void main(String[] args) {
        loadAll();
        System.out.println("=== VetSetGo: Veterinary Clinic Appointment Management ===");

        boolean running = true;
        while (running) {
            System.out.println("\n--- Welcome ---");
            System.out.println("[1] Veterinarian Login");
            System.out.println("[2] Pet Owner Login");
            System.out.println("[3] Create Veterinarian Account");
            System.out.println("[4] Create Pet Owner Account");
            System.out.println("[0] Exit");
            switch (prompt("Choice: ")) {
                case "1": if (doLogin(Role.VETERINARIAN)) sessionLoop(); break;
                case "2": if (doLogin(Role.PET_OWNER)) sessionLoop(); break;
                case "3": doRegister(Role.VETERINARIAN); break;
                case "4": doRegister(Role.PET_OWNER); break;
                case "0": running = false; break;
                default:  System.out.println("Invalid choice.");
            }
        }
        saveAll();
        System.out.println("Data saved. Goodbye!");
    }