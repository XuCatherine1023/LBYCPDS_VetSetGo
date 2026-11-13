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

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        initClinicData();
        loadSavedData();

        window.setTitle("VetSetGo | PetPulse System");
        window.setOnCloseRequest(e -> saveAllData());
        window.setScene(buildLoginScene());
        window.show();
    }

    private Scene buildLoginScene() {
        BorderPane root = new BorderPane();
        root.setStyle(BG_LIGHT);
        root.setTop(buildNavBar());

        VBox formWrap = new VBox(15);
        formWrap.setPadding(new Insets(40));
        formWrap.setStyle(CARD_STYLE);
        formWrap.setMaxWidth(400);

        Label title = new Label("Portal Login");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        Label sub = new Label("Good to see you again. Sign in to book visits.");
        sub.setWrapText(true);

        TextField userField = new TextField(); userField.setPromptText("User ID");
        PasswordField passField = new PasswordField(); passField.setPromptText("Password");

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle(BTN_PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Button signupBtn = new Button("Don't have an account? Sign up here");
        signupBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0d6efd; -fx-underline: true;");

        loginBtn.setOnAction(e -> {
            User u = auth.login(userField.getText(), passField.getText());
            if (u != null) {
                window.setScene(u.getRole() == Role.PET_OWNER ? buildOwnerDashboard(u) : buildVetDashboard(u));
            } else {
                showAlert("Login Failed", "Invalid credentials.");
            }
        });

        signupBtn.setOnAction(e -> window.setScene(buildSignupScene()));

        formWrap.getChildren().addAll(title, sub, new Label("User ID"), userField, new Label("Password"), passField, loginBtn, signupBtn);
        root.setCenter(formWrap);
        return new Scene(root, 900, 600);
    }

    private Scene buildSignupScene() {
        BorderPane root = new BorderPane();
        root.setStyle(BG_LIGHT);
        root.setTop(buildNavBar());

        VBox formWrap = new VBox(15);
        formWrap.setPadding(new Insets(40));
        formWrap.setStyle(CARD_STYLE);
        formWrap.setMaxWidth(450);

        Label title = new Label("Create a VetSetGo Account");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(Role.PET_OWNER, Role.VETERINARIAN);
        roleBox.setValue(Role.PET_OWNER);
        roleBox.setMaxWidth(Double.MAX_VALUE);

        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        TextField emailField = new TextField(); emailField.setPromptText("Email Address");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone Number");
        PasswordField passField = new PasswordField(); passField.setPromptText("Password");

        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle(BTN_PRIMARY);
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        Button backBtn = new Button("Already have an account? Login here");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0d6efd; -fx-underline: true;");

        signupBtn.setOnAction(e -> {
            if (auth.register(nameField.getText(), nameField.getText(), roleBox.getValue(), passField.getText())) {
                showAlert("Success", "Account created! Please login.");
                window.setScene(buildLoginScene());
            } else {
                showAlert("Error", "Registration failed. Check inputs.");
            }
        });
        backBtn.setOnAction(e -> window.setScene(buildLoginScene()));

        formWrap.getChildren().addAll(title, new Label("Role"), roleBox, new Label("Full Name"), nameField,
                new Label("Email Address"), emailField, new Label("Phone Number"), phoneField,
                new Label("Password"), passField, signupBtn, backBtn);
        root.setCenter(formWrap);
        return new Scene(root, 900, 650);
    }
}

