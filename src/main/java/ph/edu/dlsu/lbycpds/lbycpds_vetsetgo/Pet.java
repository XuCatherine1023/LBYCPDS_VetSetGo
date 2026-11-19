package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

import java.util.ArrayList;
import java.util.List;

public class Pet {
    private final int id;
    private String name;
    private String species;
    private int age;
    private double weight;
    private final String ownerUsername;
    private List<String> medicalHistory;

    public Pet(int id, String string, String name, String species, int age, double weight, String s, String ownerUsername) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.age = age;
        this.weight = weight;
        this.ownerUsername = ownerUsername;
        this.medicalHistory = new ArrayList<>();
    }

    public int getId()               { return id; }
    public String getName()          { return name; }
    public String getSpecies()       { return species; }
    public int getAge()              { return age; }
    public double getWeight()        { return weight; }
    public String getOwnerUsername() { return ownerUsername; }
    public List<String> getMedicalHistory() { return medicalHistory; }

    public void setName(String name)       { this.name = name; }
    public void setSpecies(String species) { this.species = species; }
    public void setAge(int age)            { this.age = age; }
    public void setWeight(double weight)   { this.weight = weight; }
    public void addMedicalRecord(String record) { this.medicalHistory.add(record); }

    public void setMedicalHistory(List<String> history) { this.medicalHistory = history; }

    @Override
    public String toString() {
        return String.format("#%d %s | %s | %d yr | %.1f kg | owner: %s",
                id, name, species, age, weight, ownerUsername);
    }
}