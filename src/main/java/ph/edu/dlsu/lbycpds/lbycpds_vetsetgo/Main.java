package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

import java.io.Console;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    /** Clinic staff code required to create a veterinarian account. Change before deploying. */
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

    private static void loadAll() {
        try {
            if (storage.usersFileExists()) {
                auth.loadUsers(storage.loadUsers());
            }
            if (auth.getUsers().isEmpty()) {
                // First run: create a default vet so someone can log in.
                auth.register("vet", "Default Veterinarian", Role.VETERINARIAN, "vet123");
                System.out.println("First run: default vet account created (username: vet, password: vet123). Change it!");
            }
            pets = storage.loadPets();
            nextPetId = pets.maxId() + 1;
        } catch (IOException e) {
            System.err.println("Could not read saved data: " + e.getMessage());
        }
    }

    private static void saveAll() {
        try {
            storage.saveUsers(auth.getUsers());
            storage.savePets(pets);
        } catch (IOException e) {
            System.err.println("ERROR: could not save data: " + e.getMessage());
        }
    }

    private static boolean doLogin(Role portal) {
        System.out.println("\n--- " + portalName(portal) + " Login ---");
        auth.resetFailedAttempts();
        while (!auth.lockedOut()) {
            String username = prompt("Username: ");
            String password = promptPassword("Password: ");
            User u = auth.login(username, password);
            if (u == null) {
                System.out.println("Invalid username or password. Attempts left: " + auth.attemptsLeft());
            } else if (u.getRole() != portal) {
                // Credentials were correct, but this account belongs to the other portal.
                auth.logout();
                System.out.println("This account is a " + portalName(u.getRole())
                        + " account. Please use the " + portalName(u.getRole()) + " Login.");
                return false;
            } else {
                System.out.println("\nLogin successful. Welcome, " + u.getFullName() + " [" + u.getRole() + "]");
                return true;
            }
        }
        System.out.println("Too many failed attempts. Returning to the main screen.");
        return false;
    }

    private static void doRegister(Role role) {
        System.out.println("\n--- Create " + portalName(role) + " Account ---");
        if (role == Role.VETERINARIAN) {
            String code = promptPassword("Clinic access code: ");
            if (!code.equals(VET_ACCESS_CODE)) {
                System.out.println("Incorrect clinic access code. Account not created.");
                return;
            }
        }
        String username = prompt("Choose a username: ");
        if (username.isEmpty() || username.contains(" ")) {
            System.out.println("Username cannot be empty or contain spaces.");
            return;
        }
        if (auth.usernameTaken(username)) {
            System.out.println("That username is already taken.");
            return;
        }
        String name = prompt("Full name: ");
        if (name.isEmpty()) {
            System.out.println("Full name cannot be empty.");
            return;
        }
        String pw = promptPassword("Password (min 6 chars): ");
        if (pw.length() < 6) {
            System.out.println("Password too short.");
            return;
        }
        if (!pw.equals(promptPassword("Confirm password: "))) {
            System.out.println("Passwords do not match.");
            return;
        }
        auth.register(username, name, role, pw);
        saveAll();                                           // persist right away
        System.out.println(portalName(role) + " account created. You can now log in.");
    }

    private static String portalName(Role role) {
        return role == Role.VETERINARIAN ? "Veterinarian" : "Pet Owner";
    }

    private static void sessionLoop() {
        while (auth.isLoggedIn()) {
            if (auth.getCurrentUser().getRole() == Role.VETERINARIAN) vetMenu();
            else ownerMenu();
        }
    }

    private static void vetMenu() {
        System.out.println("\n--- Veterinarian Menu ---");
        System.out.println("[1] View all pets");
        System.out.println("[2] Delete a pet");
        System.out.println("[3] Schedule / Queue / Records  (TODO)");
        System.out.println("[4] Delete a pet owner account");
        System.out.println("[0] Logout");
        switch (prompt("Choice: ")) {
            case "1": printPets(pets.toList()); break;
            case "2": deletePet(); break;
            case "3": System.out.println("Coming soon."); break;
            case "4": deleteOwnerAccountAsVet(); break;
            case "0": logout(); break;
            default:  System.out.println("Invalid choice.");
        }
    }

    private static void ownerMenu() {
        System.out.println("\n--- Pet Owner Menu ---");
        System.out.println("[1] View my pets");
        System.out.println("[2] Add a pet");
        System.out.println("[3] Book appointment / Fees  (TODO)");
        System.out.println("[4] Delete my account");
        System.out.println("[0] Logout");
        switch (prompt("Choice: ")) {
            case "1": printPets(pets.findByOwner(auth.getCurrentUser().getUsername())); break;
            case "2": addPet(); break;
            case "3": System.out.println("Coming soon."); break;
            case "4": deleteOwnAccount(); break;
            case "0": logout(); break;
            default:  System.out.println("Invalid choice.");
        }
    }

    private static void logout() {
        saveAll();               // persist whenever someone logs out
        auth.logout();
        System.out.println("Logged out.");
    }

    private static void deleteOwnAccount() {
        User me = auth.getCurrentUser();
        System.out.println("\nWARNING: this permanently deletes your account and all your pets' records.");
        String pw = promptPassword("Enter your password to confirm: ");
        if (!auth.verifyPassword(me, pw)) {
            System.out.println("Incorrect password. Account not deleted.");
            return;
        }
        if (!prompt("Type DELETE to confirm: ").equals("DELETE")) {
            System.out.println("Cancelled. Account not deleted.");
            return;
        }
        int removedPets = pets.removeByOwner(me.getUsername());
        auth.deleteUser(me.getUsername());
        logout();                                   // saves files and ends the session
        System.out.println("Your account and " + removedPets + " pet record(s) were deleted.");
    }

    private static void deleteOwnerAccountAsVet() {
        String username = prompt("Username of the pet owner to delete: ").toLowerCase();
        User target = auth.getUsers().get(username);
        if (target == null) {
            System.out.println("No account with that username.");
            return;
        }
        if (target.getRole() != Role.PET_OWNER) {
            System.out.println("Only pet owner accounts can be deleted here.");
            return;
        }
        int petCount = pets.findByOwner(username).size();
        System.out.println("This will delete " + target + " and " + petCount + " pet record(s).");
        if (!prompt("Type DELETE to confirm: ").equals("DELETE")) {
            System.out.println("Cancelled.");
            return;
        }
        pets.removeByOwner(username);
        auth.deleteUser(username);
        saveAll();
        System.out.println("Account deleted.");
    }

    private static void addPet() {
        try {
            String name = prompt("Pet name: ");
            String species = prompt("Species: ");
            int age = Integer.parseInt(prompt("Age (years): "));
            double weight = Double.parseDouble(prompt("Weight (kg): "));
            if (name.isEmpty() || species.isEmpty() || age < 0 || weight <= 0) {
                System.out.println("Invalid pet details.");
                return;
            }
            Pet p = new Pet(nextPetId++, name, species, age, weight, auth.getCurrentUser().getUsername());
            pets.add(p);
            System.out.println("Added: " + p);
        } catch (NumberFormatException e) {
            System.out.println("Age and weight must be numbers.");
        }
    }

    private static void deletePet() {
        try {
            int id = Integer.parseInt(prompt("Pet ID to delete: "));
            System.out.println(pets.removeById(id) ? "Deleted." : "No pet with that ID.");
        } catch (NumberFormatException e) {
            System.out.println("ID must be a number.");
        }
    }

    private static void printPets(List<Pet> list) {
        if (list.isEmpty()) { System.out.println("No pets found."); return; }
        for (Pet p : list) System.out.println(p);
    }

    private static String prompt(String msg) {
        System.out.print(msg);
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    /** Hides typing when run from a real terminal; falls back to plain input in IDEs. */
    private static String promptPassword(String msg) {
        Console c = System.console();
        if (c != null) return new String(c.readPassword(msg));
        return prompt(msg);
    }
}