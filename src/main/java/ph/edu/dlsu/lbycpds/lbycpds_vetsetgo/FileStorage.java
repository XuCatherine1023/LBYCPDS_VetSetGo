package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

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
                Pet p = new Pet(Integer.parseInt(f[0]), f[1], f[2], Integer.parseInt(f[3]), Double.parseDouble(f[4]), f[5]);
                if (!f[6].isEmpty()) {
                    p.setMedicalHistory(new ArrayList<>(Arrays.asList(f[6].split(";;"))));
                }
                list.add(p);
            } catch (NumberFormatException bad) {
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

    public List<Appointment> loadAppointments() throws IOException {
        List<Appointment> list = new ArrayList<>();
        for (String[] f : readRecords(appointmentsFile, 8)) {
            try {
                if (f.length >= 11) {
                    list.add(new Appointment(f[0], f[1], f[2], Integer.parseInt(f[3]), f[4], Integer.parseInt(f[5]), f[6], f[7], Double.parseDouble(f[8]), f[9], f[10]));
                } else if (f.length == 10) {
                    list.add(new Appointment("", f[0], f[1], Integer.parseInt(f[2]), f[3], Integer.parseInt(f[4]), f[5], f[6], Double.parseDouble(f[7]), f[8], f[9]));
                } else if (f.length == 8) {
                    list.add(new Appointment(f[0], f[1], f[2], Integer.parseInt(f[3]), f[4], Integer.parseInt(f[5]), f[6], f[7]));
                } else if (f.length == 7) {
                    list.add(new Appointment("", f[0], f[1], Integer.parseInt(f[2]), f[3], Integer.parseInt(f[4]), f[5], f[6]));
                }
            } catch (Exception e) {
            }
        }
        return list;
    }

    public void saveAppointments(List<Appointment> appointments) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (Appointment a : appointments) {
            rows.add(new String[]{a.getOwnerUsername(), a.getPetName(), a.getCaseType(), String.valueOf(a.getSeverityLevel()), a.getDate(), String.valueOf(a.getTimeSlot()), a.getVetName(), a.getReason(), String.valueOf(a.getPrice()), a.getPaymentMethod(), a.getStatus()});
        }
        writeRecords(appointmentsFile, rows);
    }

    private List<String[]> readRecords(Path file, int expectedFields) throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) return rows;
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitEscaped(line);
                if (f.length >= expectedFields) rows.add(f);
            }
        }
        return rows;
    }

    private void writeRecords(Path file, List<String[]> rows) throws IOException {
        Files.createDirectories(dir);
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            for (String[] row : rows) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append('|');
                    sb.append(escape(row[i]));
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        }
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '|':  sb.append("\\p");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    static String[] splitEscaped(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                char n = line.charAt(++i);
                switch (n) {
                    case 'p':  cur.append('|');  break;
                    case 'n':  cur.append('\n'); break;
                    case 'r':  cur.append('\r'); break;
                    default:   cur.append(n);
                }
            } else if (c == '|') {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }
}