import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileStorage {
    private final Path dir;
    private final Path usersFile;
    private final Path petsFile;

    public FileStorage(String directory) {
        this.dir = Paths.get(directory);
        this.usersFile = dir.resolve("users.txt");
        this.petsFile  = dir.resolve("pets.txt");
    }

    public boolean usersFileExists() { return Files.exists(usersFile); }


    public Map<String, User> loadUsers() throws IOException {
        Map<String, User> out = new HashMap<>();
        for (String[] f : readRecords(usersFile, 5)) {
            try {
                User u = new User(f[0], f[1], Role.fromString(f[2]), f[3], f[4]);
                out.put(u.getUsername(), u);
            } catch (IllegalArgumentException bad) {
                System.err.println("Skipping corrupt user record: " + String.join("|", f));
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
        for (String[] f : readRecords(petsFile, 6)) {
            try {
                list.add(new Pet(Integer.parseInt(f[0]), f[1], f[2],
                        Integer.parseInt(f[3]), Double.parseDouble(f[4]), f[5]));
            } catch (NumberFormatException bad) {
                System.err.println("Skipping corrupt pet record: " + String.join("|", f));
            }
        }
        return list;
    }

    public void savePets(PetLinkedList pets) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (Pet p : pets.toList())
            rows.add(new String[]{String.valueOf(p.getId()), p.getName(), p.getSpecies(),
                    String.valueOf(p.getAge()), String.valueOf(p.getWeight()),
                    p.getOwnerUsername()});
        writeRecords(petsFile, rows);
    }



    private List<String[]> readRecords(Path file, int expectedFields) throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) return rows;
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitEscaped(line);
                if (f.length == expectedFields) rows.add(f);
                else System.err.println("Skipping malformed line in " + file.getFileName() + ": " + line);
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
