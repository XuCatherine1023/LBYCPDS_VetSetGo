package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

public class Appointment implements Comparable<Appointment> {
    private String petName;
    private String caseType;
    private int severityLevel;
    private int timeSlot;
    private int triageScore;
    private long arrivalTimestamp;

    public Appointment(String petName, String caseType, int severityLevel, int timeSlot) {
        this.petName = petName;
        this.caseType = caseType;
        this.severityLevel = severityLevel;
        this.timeSlot = timeSlot;
        this.arrivalTimestamp = System.nanoTime();
        calculateTriageScore();
    }

    private void calculateTriageScore() {
        int caseTypeWeight = 0;
        if (caseType.equalsIgnoreCase("Surgical")) caseTypeWeight = 60;
        else if (caseType.equalsIgnoreCase("Urgent")) caseTypeWeight = 40;

        // Triage Scoring formula
        this.triageScore = caseTypeWeight + (severityLevel * 10);
    }

    public int getTriageScore() { return triageScore; }
    public long getArrivalTimestamp() { return arrivalTimestamp; }
    public int getTimeSlot() { return timeSlot; }
    public String getPetName() { return petName; }
    public String getCaseType() { return caseType; }
    public int getSeverityLevel() { return severityLevel; }

    @Override
    public int compareTo(Appointment other) {
        if (this.triageScore != other.triageScore) {
            return Integer.compare(this.triageScore, other.triageScore);
        }

        return Long.compare(other.arrivalTimestamp, this.arrivalTimestamp);
    }
}
