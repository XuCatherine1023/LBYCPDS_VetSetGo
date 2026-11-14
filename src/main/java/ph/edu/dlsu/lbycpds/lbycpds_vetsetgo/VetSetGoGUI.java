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

    private Scene buildOwnerDashboard(User user) {
        BorderPane root = new BorderPane();
        root.setStyle(BG_LIGHT);
        root.setTop(buildNavBar(user));

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox userDetails = new VBox(5);
        Label welcome = new Label("Welcome, " + user.getFullName());
        welcome.setFont(Font.font("System", FontWeight.BOLD, 22));
        userDetails.getChildren().add(welcome);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button undoBtn = new Button("Undo Last Action"); undoBtn.setStyle(BTN_OUTLINE);
        undoBtn.setOnAction(e -> {
            if (!undoStack.isEmpty()) { undoStack.pop().run(); showAlert("Success", "Last action undone."); window.setScene(buildOwnerDashboard(user)); }
            else showAlert("Notice", "Nothing to undo.");
        });

        header.getChildren().addAll(userDetails, spacer, undoBtn);

        HBox split = new HBox(20);
        VBox leftCol = new VBox(15); leftCol.setPrefWidth(300);
        VBox rightCol = new VBox(15); HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox hoursCard = new VBox(10);
        hoursCard.setStyle(CARD_STYLE); hoursCard.setPadding(new Insets(15));
        Label hTitle = new Label("🕒 Clinic Hours"); hTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        VBox hList = new VBox(5);

        for (int i = 0; i < ClinicStructures.CLINIC_HOURS.length; i++) {
            hList.getChildren().add(new Label(ClinicStructures.CLINIC_HOURS[i]));
        }
        hoursCard.getChildren().addAll(hTitle, hList);

        VBox regPetCard = new VBox(10);
        regPetCard.setStyle(CARD_STYLE);
        regPetCard.setPadding(new Insets(15));
        Label regTitle = new Label("🐾 Register New Pet"); regTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        TextField pName = new TextField(); pName.setPromptText("Pet Name");
        TextField pSpecies = new TextField(); pSpecies.setPromptText("Species (e.g., Dog)");
        TextField pAge = new TextField(); pAge.setPromptText("Age");
        TextField pWeight = new TextField(); pWeight.setPromptText("Weight (kg)");
        Button addPetBtn = new Button("Add Pet"); addPetBtn.setStyle(BTN_PRIMARY); addPetBtn.setMaxWidth(Double.MAX_VALUE);
        regPetCard.getChildren().addAll(regTitle, pName, pSpecies, pAge, pWeight, addPetBtn);
        leftCol.getChildren().addAll(hoursCard, regPetCard);

        Label petsTitle = new Label("🐾 My Pets"); petsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        TilePane petsGrid = new TilePane(15, 15);
        petsGrid.setPrefColumns(2);

        Runnable refreshPets = () -> {
            petsGrid.getChildren().clear();
            List<Pet> myPets = petList.findByOwner(user.getUsername());
            for (Pet p : myPets) {
                VBox card = new VBox(8); card.setStyle(CARD_STYLE); card.setPadding(new Insets(15));
                Label n = new Label(p.getName()); n.setStyle("-fx-text-fill: #0d6efd; -fx-font-weight: bold; -fx-font-size: 16px;");
                Label s = new Label("Species: " + p.getSpecies());
                Button viewBtn = new Button("View Profile"); viewBtn.setStyle(BTN_PRIMARY);
                viewBtn.setOnAction(e -> {
                    recentViewedStack.push(p);
                    window.setScene(buildPetProfileScene(p, user));
                });
                card.getChildren().addAll(n, s, viewBtn);
                petsGrid.getChildren().add(card);
            }
        };
        refreshPets.run();
        rightCol.getChildren().addAll(petsTitle, petsGrid);
        split.getChildren().addAll(leftCol, rightCol);

        VBox bookCard = new VBox(15);
        bookCard.setStyle(CARD_STYLE);
        bookCard.setPadding(new Insets(20));
        Label bookTitle = new Label("📅 Request Routine Appointment"); bookTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        HBox bookForm = new HBox(15);
        TextField aptPet = new TextField(); aptPet.setPromptText("Pet Name");
        ComboBox<Integer> slotBox = new ComboBox<>();
        for (int s : clinicTimeSlots) slotBox.getItems().add(s);
        Button confirmBook = new Button("Confirm Booking"); confirmBook.setStyle(BTN_PRIMARY);
        bookForm.getChildren().addAll(aptPet, slotBox, confirmBook);
        bookCard.getChildren().addAll(bookTitle, bookForm);

        VBox myBookingsCard = new VBox(15);
        myBookingsCard.setStyle(CARD_STYLE); myBookingsCard.setPadding(new Insets(20));
        Label myBookingsTitle = new Label("📋 My Upcoming Bookings"); myBookingsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        TableView<Appointment> ownerTable = new TableView<>();
        TableColumn<Appointment, Integer> timeColO = new TableColumn<>("Time Slot"); timeColO.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        TableColumn<Appointment, String> petColO = new TableColumn<>("Pet Name"); petColO.setCellValueFactory(new PropertyValueFactory<>("petName"));
        TableColumn<Appointment, String> typeColO = new TableColumn<>("Case Type"); typeColO.setCellValueFactory(new PropertyValueFactory<>("caseType"));
        ownerTable.getColumns().addAll(timeColO, petColO, typeColO);
        ownerTable.setPrefHeight(200);

        Runnable refreshOwnerTable = () -> {
            List<String> myPetNames = new ArrayList<>();
            for (Pet p : petList.findByOwner(user.getUsername())) myPetNames.add(p.getName());
            List<Appointment> filtered = new ArrayList<>();
            for (Appointment a : allAppointments) {
                if (myPetNames.contains(a.getPetName())) filtered.add(a);
            }
            if (!filtered.isEmpty()) ClinicAlgorithms.quickSortAppointments(filtered, 0, filtered.size() - 1);
            ownerTable.getItems().setAll(filtered);
        };
        refreshOwnerTable.run();
        myBookingsCard.getChildren().addAll(myBookingsTitle, ownerTable);

        confirmBook.setOnAction(e -> {
            int targetSlot = slotBox.getValue();
            int idx = ClinicAlgorithms.binarySearchTimeSlot(clinicTimeSlots, targetSlot);
            if (idx == -1) { showAlert("Error", "Invalid Time Slot."); return; }
            Appointment apt = new Appointment(aptPet.getText(), "Routine", 1, targetSlot);
            appointmentQueue.enqueue(apt);
            allAppointments.add(apt);
            refreshOwnerTable.run();

            undoStack.push(() -> {
                allAppointments.remove(apt);
                refreshOwnerTable.run();
            });
            showAlert("Success", "Routine appointment queued for slot " + targetSlot);
        });

        addPetBtn.setOnAction(e -> {
            Pet p = new Pet(nextPetId++, pName.getText(), pSpecies.getText(), "", Integer.parseInt(pAge.getText()), Double.parseDouble(pWeight.getText()), "", user.getUsername());
            petList.add(p);
            petAvlTree.insert(p);
            speciesTree.addSpeciesAndBreed(p.getSpecies(), "Unknown");
            refreshPets.run();

            undoStack.push(() -> {
                petList.removeById(p.getId());
                refreshPets.run();
            });
        });

        content.getChildren().addAll(header, split, bookCard, myBookingsCard);
        root.setCenter(new ScrollPane(content));
        return new Scene(root, 1000, 850);
    }

}