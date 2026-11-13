package ph.edu.dlsu.lbycpds.lbycpds_vetsetgodemo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class FileStorage {
    private final Path dir;
    private final Path usersFile;
    private final Path petsFile;
    private final Path appointmentsFile;

    public FileStorage(String directory) {
        this.dir = Paths.get(directory);
        this.usersFile = dir.resolve("users.txt");
        this.petsFile  = dir.resolve("pets.txt");
        this.appointmentsFile = dir.resolve("appointments.txt");
    }

    public boolean usersFileExists() { return Files.exists(usersFile); }

    public Map<String, User> loadUsers() throws IOException {
        Map<String, User> out = new HashMap<>();
        for (String[] f : readRecords(usersFile, 5)) {
            try {
                User u = new User(f[0], f[1], Role.fromString(f[2]), f[3], f[4]);
                out.put(u.getUsername(), u);
            } catch (IllegalArgumentException bad) {
                System.err.println("Skipping corrupt user: " + String.join("|", f));
            }
        }
        return out;
    }

    public void saveUsers(Map<String, User> users) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (User u : users.values())
            rows.add(new String[]{u.getUsername(), u.getFullName(), u.getRole().name(),
                    u.getSalt(), u.getPasswordHash()});
        writeRecords(usersFile, rows);
    }

    public PetLinkedList loadPets() throws IOException {
        PetLinkedList list = new PetLinkedList();
        for (String[] f : readRecords(petsFile, 7)) {
            try {
                Pet p = new Pet(Integer.parseInt(f[0]), f[1], f[2], "", Integer.parseInt(f[3]), Double.parseDouble(f[4]), "", f[5]);
                if (!f[6].isEmpty()) {
                    p.setMedicalHistory(new ArrayList<>(Arrays.asList(f[6].split(";;"))));
                }
                list.add(p);
            } catch (NumberFormatException bad) {
                System.err.println("Skipping corrupt pet: " + String.join("|", f));
            }
        }
        return list;
    }

    public void savePets(PetLinkedList pets) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (Pet p : pets.toList()) {
            String historyStr = String.join(";;", p.getMedicalHistory());
            rows.add(new String[]{String.valueOf(p.getId()), p.getName(), p.getSpecies(),
                    String.valueOf(p.getAge()), String.valueOf(p.getWeight()),
                    p.getOwnerUsername(), historyStr});
        }
        writeRecords(petsFile, rows);
    }
