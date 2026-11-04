import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final Map<String, User> users = new HashMap<>();

    public void loadUsers(Map<String, User> loaded) {
        users.clear();
        users.putAll(loaded);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public boolean usernameTaken(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public boolean register(String username, String fullName, Role role, String password) {
        String key = username.trim().toLowerCase();
        if (key.isEmpty() || password.isEmpty() || users.containsKey(key)) return false;
        String salt = newSalt();
        users.put(key, new User(key, fullName.trim(), role, salt, hash(password, salt)));
        return true;
    }

    public boolean deleteUser(String username) {
        return users.remove(username.trim().toLowerCase()) != null;
    }

    public boolean verifyPassword(User user, String password) {
        return constantTimeEquals(hash(password, user.getSalt()), user.getPasswordHash());
    }

    public static String newSalt() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] d = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}