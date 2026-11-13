package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

public class User {
    private final String username;
    private final String fullName;
    private final Role role;
    private final String salt;
    private final String passwordHash;

    public User(String username, String fullName, Role role, String salt, String passwordHash) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public String getUsername()     { return username; }
    public String getFullName()     { return fullName; }
    public Role getRole()           { return role; }
    public String getSalt()         { return salt; }
    public String getPasswordHash() { return passwordHash; }

    @Override
    public String toString() {
        return fullName + " (" + username + ", " + role + ")";
    }
}