public class Pet {
    private final int id;
    private String name;
    private String species;
    private int age;
    private double weight;
    private final String ownerUsername;

    public Pet(int id, String name, String species, int age, double weight, String ownerUsername) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.age = age;
        this.weight = weight;
        this.ownerUsername = ownerUsername;
    }

    public int getId()               { return id; }
    public String getName()          { return name; }
    public String getSpecies()       { return species; }
    public int getAge()              { return age; }
    public double getWeight()        { return weight; }
    public String getOwnerUsername() { return ownerUsername; }

    public void setName(String name)       { this.name = name; }
    public void setSpecies(String species) { this.species = species; }
    public void setAge(int age)            { this.age = age; }
    public void setWeight(double weight)   { this.weight = weight; }

    @Override
    public String toString() {
        return String.format("#%d %s | %s | %d yr | %.1f kg | owner: %s",
                id, name, species, age, weight, ownerUsername);
    }
}