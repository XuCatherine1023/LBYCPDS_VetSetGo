package ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.util.*;

public class VetSetGoGUI extends Application {

    private static final String BG_LIGHT = "-fx-background-color: #f8f9fa;";
    private static final String BTN_PRIMARY = "-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;";
    private static final String BTN_OUTLINE = "-fx-background-color: transparent; -fx-border-color: #0d6efd; -fx-text-fill: #0d6efd; -fx-background-radius: 5; -fx-border-radius: 5;";
    private static final String BTN_DANGER = "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5;";
    private static final String CARD_STYLE = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);";

    private Stage window;
    private final FileStorage storage = new FileStorage("data");
    private final AuthService auth = new AuthService();
    private PetLinkedList petList = new PetLinkedList();
    private int nextPetId = 1;

    private final ClinicStructures.PriorityHeap priorityHeap = new ClinicStructures.PriorityHeap();
    private final ClinicStructures.AppointmentQueue appointmentQueue = new ClinicStructures.AppointmentQueue();
    private final List<Appointment> allAppointments = new ArrayList<>();
    private final ClinicStructures.RecordStack recentViewedStack = new ClinicStructures.RecordStack();
    private final Deque<Runnable> undoStack = new ArrayDeque<>();

    private final Map<String, Double> serviceFees = new HashMap<>();
    private final ClinicStructures.SymptomGraph symptomGraph = new ClinicStructures.SymptomGraph();
    private final ClinicStructures.PetAVLTree petAvlTree = new ClinicStructures.PetAVLTree();
    private final ClinicStructures.SpeciesBreedTree speciesTree = new ClinicStructures.SpeciesBreedTree();

    private final int[] clinicTimeSlots = {900, 1000, 1100, 1300, 1400, 1500, 1600};

