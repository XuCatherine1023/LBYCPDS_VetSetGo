package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

public class Appointment implements Comparable<Appointment> {
    private String ownerUsername;
    private String petName;
    private String caseType;
    private int severityLevel;
    private String date;
    private int timeSlot;
    private String vetName;
    private String reason;
    private int triageScore;
    private long arrivalTimestamp;
    private double price;
    private String paymentMethod;
    private String status;

    public Appointment(String ownerUsername, String petName, String caseType, int severityLevel, String date, int timeSlot, String vetName, String reason) {
        this.ownerUsername = ownerUsername;
        this.petName = petName;
        this.caseType = caseType;
        this.severityLevel = severityLevel;
        this.date = date;
        this.timeSlot = timeSlot;
        this.vetName = vetName;
        this.reason = reason;
        this.arrivalTimestamp = System.nanoTime();
        this.price = 0.0;
        this.paymentMethod = "Cash";
        this.status = "Pending";
        calculateTriageScore();
    }

    public Appointment(String ownerUsername, String petName, String caseType, int severityLevel, String date, int timeSlot, String vetName, String reason, double price, String paymentMethod, String status) {
        this.ownerUsername = ownerUsername;
        this.petName = petName;
        this.caseType = caseType;
        this.severityLevel = severityLevel;
        this.date = date;
        this.timeSlot = timeSlot;
        this.vetName = vetName;
        this.reason = reason;
        this.arrivalTimestamp = System.nanoTime();
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.status = status;
        calculateTriageScore();
    }

    private void calculateTriageScore() {
        int caseTypeWeight = 0;
        if (caseType.equalsIgnoreCase("Surgical")) caseTypeWeight = 60;
        else if (caseType.equalsIgnoreCase("Urgent")) caseTypeWeight = 40;
        this.triageScore = caseTypeWeight + (severityLevel * 10);
    }

    public int getTriageScore() { return triageScore; }
    public long getArrivalTimestamp() { return arrivalTimestamp; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public String getDate() { return date; }
    public int getTimeSlot() { return timeSlot; }
    public String getPetName() { return petName; }
    public String getCaseType() { return caseType; }
    public int getSeverityLevel() { return severityLevel; }
    public String getVetName() { return vetName; }
    public String getReason() { return reason; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public int compareTo(Appointment other) {
        if (this.triageScore != other.triageScore) {
            return Integer.compare(this.triageScore, other.triageScore);
        }
        return Long.compare(other.arrivalTimestamp, this.arrivalTimestamp);
    }
}
