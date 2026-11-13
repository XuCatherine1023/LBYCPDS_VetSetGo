package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

public enum Role {
    VETERINARIAN,
    PET_OWNER;

    public static Role fromString(String s) {
        return Role.valueOf(s.trim().toUpperCase());
    }
}
