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

    private static final String BG_LIGHT = "-fx-background-color: #F3E5F5;";
    private static final String BTN_PRIMARY = "-fx-background-color: #512DA8; -fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-background-radius: 5;";
    private static final String BTN_OUTLINE = "-fx-background-color: transparent; -fx-border-color: #512DA8; -fx-text-fill: #512DA8; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-radius: 5;";
    private static final String BTN_DANGER = "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5;";
    private static final String CARD_STYLE = "-fx-background-color: white; -fx-border-color: #D1C4E9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);";

    private Stage window;
    private final FileStorage storage = new FileStorage("data");
    private final AuthService auth = new AuthService();
    private PetLinkedList petList = new PetLinkedList();
    private int nextPetId = 1;

    private final List<Appointment> allAppointments = new ArrayList<>();
    private final ClinicStructures.RecordStack recentViewedStack = new ClinicStructures.RecordStack();
    private final Deque<Runnable> undoStack = new ArrayDeque<>();

    private final Map<String, Double> serviceFees = new HashMap<>();
    private final ClinicStructures.SymptomGraph symptomGraph = new ClinicStructures.SymptomGraph();
    private final ClinicStructures.PetAVLTree petAvlTree = new ClinicStructures.PetAVLTree();
    private final ClinicStructures.SpeciesBreedTree speciesTree = new ClinicStructures.SpeciesBreedTree();

    private final int[] clinicTimeSlots = {900, 1000, 1100, 1300, 1400, 1500, 1600};

    private Appointment activePatient = null;

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
        title.setStyle("-fx-text-fill: #512DA8;");
        Label sub = new Label("Good to see you again. Sign in to book visits.");
        sub.setWrapText(true);

        TextField userField = new TextField(); userField.setPromptText("User ID");
        PasswordField passField = new PasswordField(); passField.setPromptText("Password");

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle(BTN_PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Button signupBtn = new Button("Don't have an account? Sign up here");
        signupBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #512DA8; -fx-underline: true; -fx-font-weight: bold;");

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
        title.setStyle("-fx-text-fill: #512DA8;");

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
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #512DA8; -fx-underline: true; -fx-font-weight: bold;");

        signupBtn.setOnAction(e -> {
            if (auth.register(nameField.getText(), nameField.getText(), roleBox.getValue(), passField.getText())) {
                saveAllData();
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
        welcome.setStyle("-fx-text-fill: #512DA8;");
        userDetails.getChildren().add(welcome);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button undoBtn = new Button("Undo Last Action"); undoBtn.setStyle(BTN_OUTLINE);
        undoBtn.setOnAction(e -> {
            if (!undoStack.isEmpty()) { undoStack.pop().run(); saveAllData(); showAlert("Success", "Last action undone."); window.setScene(buildOwnerDashboard(user)); }
            else showAlert("Notice", "Nothing to undo.");
        });

        header.getChildren().addAll(userDetails, spacer, undoBtn);

        HBox split = new HBox(20);
        VBox leftCol = new VBox(15); leftCol.setPrefWidth(300);
        VBox rightCol = new VBox(15); HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox hoursCard = new VBox(10);
        hoursCard.setStyle(CARD_STYLE); hoursCard.setPadding(new Insets(15));
        Label hTitle = new Label("🕒 Clinic Hours"); hTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #512DA8;");
        VBox hList = new VBox(5);

        for (int i = 0; i < ClinicStructures.CLINIC_HOURS.length; i++) {
            hList.getChildren().add(new Label(ClinicStructures.CLINIC_HOURS[i]));
        }
        hoursCard.getChildren().addAll(hTitle, hList);

        VBox regPetCard = new VBox(10);
        regPetCard.setStyle(CARD_STYLE);
        regPetCard.setPadding(new Insets(15));
        Label regTitle = new Label("🐾 Register New Pet"); regTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #512DA8;");
        TextField pName = new TextField(); pName.setPromptText("Pet Name");
        TextField pSpecies = new TextField(); pSpecies.setPromptText("Species (e.g., Dog)");
        TextField pAge = new TextField(); pAge.setPromptText("Age");
        TextField pWeight = new TextField(); pWeight.setPromptText("Weight (kg)");
        Button addPetBtn = new Button("Add Pet"); addPetBtn.setStyle(BTN_PRIMARY); addPetBtn.setMaxWidth(Double.MAX_VALUE);
        regPetCard.getChildren().addAll(regTitle, pName, pSpecies, pAge, pWeight, addPetBtn);
        leftCol.getChildren().addAll(hoursCard, regPetCard);

        Label petsTitle = new Label("🐾 My Pets"); petsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        petsTitle.setStyle("-fx-text-fill: #512DA8;");
        TilePane petsGrid = new TilePane(15, 15);
        petsGrid.setPrefColumns(2);

        Runnable refreshPets = () -> {
            petsGrid.getChildren().clear();
            List<Pet> myPets = petList.findByOwner(user.getUsername());
            for (Pet p : myPets) {
                VBox card = new VBox(8); card.setStyle(CARD_STYLE); card.setPadding(new Insets(15));
                Label n = new Label(p.getName()); n.setStyle("-fx-text-fill: #512DA8; -fx-font-weight: bold; -fx-font-size: 16px;");
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
        bookTitle.setStyle("-fx-text-fill: #512DA8;");

        HBox bookForm1 = new HBox(15);
        TextField aptPet = new TextField(); aptPet.setPromptText("Pet Name");
        DatePicker datePicker = new DatePicker(); datePicker.setPromptText("Select Date");
        ComboBox<Integer> slotBox = new ComboBox<>();
        for (int s : clinicTimeSlots) slotBox.getItems().add(s);
        slotBox.setPromptText("Time Slot");

        HBox bookForm2 = new HBox(15);
        ComboBox<String> vetBox = new ComboBox<>();
        for (User u : auth.getUsers().values()) {
            if (u.getRole() == Role.VETERINARIAN) {
                vetBox.getItems().add("Dr. " + u.getFullName());
            }
        }
        vetBox.setPromptText("Select Veterinarian");

        ComboBox<String> reasonBox = new ComboBox<>();
        reasonBox.getItems().addAll("Consultation", "Vaccination", "Surgery");
        reasonBox.setPromptText("Reason for Visit");

        ComboBox<String> paymentBox = new ComboBox<>();
        paymentBox.getItems().addAll("Cash", "Card", "GCash (09663657893)");
        paymentBox.setPromptText("Payment Method");

        Button confirmBook = new Button("Confirm Booking"); confirmBook.setStyle(BTN_PRIMARY);

        bookForm1.getChildren().addAll(aptPet, datePicker, slotBox);
        bookForm2.getChildren().addAll(vetBox, reasonBox, paymentBox, confirmBook);
        bookCard.getChildren().addAll(bookTitle, bookForm1, bookForm2);

        VBox myBookingsCard = new VBox(15);
        myBookingsCard.setStyle(CARD_STYLE); myBookingsCard.setPadding(new Insets(20));
        Label myBookingsTitle = new Label("📋 My Bookings"); myBookingsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        myBookingsTitle.setStyle("-fx-text-fill: #512DA8;");

        TableView<Appointment> ownerTable = new TableView<>();
        TableColumn<Appointment, String> dateColO = new TableColumn<>("Date"); dateColO.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Appointment, Integer> timeColO = new TableColumn<>("Time"); timeColO.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        TableColumn<Appointment, String> petColO = new TableColumn<>("Pet Name"); petColO.setCellValueFactory(new PropertyValueFactory<>("petName"));
        TableColumn<Appointment, String> vetColO = new TableColumn<>("Veterinarian"); vetColO.setCellValueFactory(new PropertyValueFactory<>("vetName"));
        TableColumn<Appointment, String> typeColO = new TableColumn<>("Reason"); typeColO.setCellValueFactory(new PropertyValueFactory<>("reason"));
        TableColumn<Appointment, String> pmColO = new TableColumn<>("Payment"); pmColO.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        TableColumn<Appointment, Double> priceColO = new TableColumn<>("Price (₱)"); priceColO.setCellValueFactory(new PropertyValueFactory<>("price"));
        ownerTable.getColumns().addAll(dateColO, timeColO, petColO, vetColO, typeColO, pmColO, priceColO);
        ownerTable.setPrefHeight(200);

        Runnable refreshOwnerTable = () -> {
            List<Appointment> filtered = new ArrayList<>();
            for (Appointment a : allAppointments) {
                if (user.getUsername().equals(a.getOwnerUsername())) filtered.add(a);
            }
            if (!filtered.isEmpty()) ClinicAlgorithms.quickSortAppointments(filtered, 0, filtered.size() - 1);
            ownerTable.getItems().setAll(filtered);
        };
        refreshOwnerTable.run();
        myBookingsCard.getChildren().addAll(myBookingsTitle, ownerTable);

        confirmBook.setOnAction(e -> {
            if(datePicker.getValue() == null || vetBox.getValue() == null || slotBox.getValue() == null || aptPet.getText().isEmpty() || reasonBox.getValue() == null || paymentBox.getValue() == null) {
                showAlert("Error", "Please fill in all booking fields.");
                return;
            }
            int targetSlot = slotBox.getValue();
            int idx = ClinicAlgorithms.binarySearchTimeSlot(clinicTimeSlots, targetSlot);
            if (idx == -1) { showAlert("Error", "Invalid Time Slot."); return; }

            boolean conflict = false;
            for(Appointment a : allAppointments) {
                if(a.getVetName().equals(vetBox.getValue()) && a.getDate().equals(datePicker.getValue().toString()) && a.getTimeSlot() == targetSlot) {
                    conflict = true;
                    break;
                }
            }
            if(conflict) {
                showAlert("Error", "Time slot already booked for this veterinarian.");
                return;
            }

            Appointment apt = new Appointment(user.getUsername(), aptPet.getText(), "Routine", 1, datePicker.getValue().toString(), targetSlot, vetBox.getValue(), reasonBox.getValue());
            apt.setPaymentMethod(paymentBox.getValue());
            allAppointments.add(apt);
            saveAllData();
            refreshOwnerTable.run();

            undoStack.push(() -> {
                allAppointments.remove(apt);
                saveAllData();
                refreshOwnerTable.run();
            });

            Alert receipt = new Alert(Alert.AlertType.INFORMATION);
            receipt.setTitle("Booking Receipt");
            receipt.setHeaderText("Transaction Successful");
            receipt.setContentText("Pet: " + apt.getPetName() + "\nVet: " + apt.getVetName() + "\nDate: " + apt.getDate() + "\nTime: " + apt.getTimeSlot() + "\nReason: " + apt.getReason() + "\nPayment Method: " + apt.getPaymentMethod());
            receipt.showAndWait();
        });

        addPetBtn.setOnAction(e -> {
            Pet p = new Pet(nextPetId++, pName.getText(), pSpecies.getText(), Integer.parseInt(pAge.getText()), Double.parseDouble(pWeight.getText()), user.getUsername());
            petList.add(p);
            petAvlTree.insert(p);
            speciesTree.addSpeciesAndBreed(p.getSpecies(), "Unknown");
            saveAllData();
            refreshPets.run();

            undoStack.push(() -> {
                petList.removeById(p.getId());
                saveAllData();
                refreshPets.run();
            });
        });

        content.getChildren().addAll(header, split, bookCard, myBookingsCard);
        root.setCenter(new ScrollPane(content));
        return new Scene(root, 1050, 850);
    }

    private Scene buildPetProfileScene(Pet pet, User user) {
        BorderPane root = new BorderPane();
        root.setStyle(BG_LIGHT);
        root.setTop(buildNavBar(user));

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        HBox header = new HBox(20);
        Label title = new Label("🐾 " + pet.getName() + "'s Profile");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #512DA8;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button backBtn = new Button("← Back"); backBtn.setStyle(BTN_OUTLINE);
        backBtn.setOnAction(e -> window.setScene(user.getRole() == Role.PET_OWNER ? buildOwnerDashboard(user) : buildVetDashboard(user)));

        Button prevViewedBtn = new Button("View Previous Record"); prevViewedBtn.setStyle(BTN_OUTLINE);
        prevViewedBtn.setOnAction(e -> {
            if(!recentViewedStack.isEmpty()) window.setScene(buildPetProfileScene(recentViewedStack.pop(), user));
        });

        header.getChildren().addAll(title, spacer, prevViewedBtn, backBtn);

        VBox stats = new VBox(10);
        stats.setStyle(CARD_STYLE + "-fx-border-color: #FFC107; -fx-border-width: 2;");
        stats.setPadding(new Insets(15));

        TextField updateName = new TextField(pet.getName());
        TextField updateAge = new TextField(String.valueOf(pet.getAge()));
        TextField updateWeight = new TextField(String.valueOf(pet.getWeight()));

        Button updateBtn = new Button("Update Details"); updateBtn.setStyle(BTN_PRIMARY);
        updateBtn.setOnAction(e -> {
            String oldName = pet.getName();
            pet.setName(updateName.getText());
            pet.setAge(Integer.parseInt(updateAge.getText()));
            pet.setWeight(Double.parseDouble(updateWeight.getText()));
            saveAllData();

            undoStack.push(() -> { pet.setName(oldName); saveAllData(); window.setScene(buildPetProfileScene(pet, user)); });
            showAlert("Success", "Record updated.");
        });

        Button deleteBtn = new Button("Delete Pet"); deleteBtn.setStyle(BTN_DANGER);
        deleteBtn.setOnAction(e -> {
            petList.removeById(pet.getId());
            saveAllData();
            undoStack.push(() -> { petList.add(pet); saveAllData(); });
            showAlert("Deleted", "Pet removed from system.");
            window.setScene(user.getRole() == Role.PET_OWNER ? buildOwnerDashboard(user) : buildVetDashboard(user));
        });

        HBox crudActions = new HBox(10, updateBtn, deleteBtn);
        stats.getChildren().addAll(
                new Label("Name:"), updateName,
                new Label("Species: " + pet.getSpecies()),
                new Label("Age:"), updateAge,
                new Label("Weight (kg):"), updateWeight,
                crudActions
        );

        Label medTitle = new Label("🩺 Medical History"); medTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        medTitle.setStyle("-fx-text-fill: #512DA8;");
        VBox medList = new VBox(10);
        for (String log : pet.getMedicalHistory()) {
            Label l = new Label("• " + log);
            l.setStyle(CARD_STYLE); l.setPadding(new Insets(10)); l.setMaxWidth(Double.MAX_VALUE);
            medList.getChildren().add(l);
        }

        if (user.getRole() == Role.VETERINARIAN) {
            HBox addMedForm = new HBox(10);
            TextField logField = new TextField(); logField.setPromptText("Diagnosis Notes...");
            Button addLogBtn = new Button("Save Record"); addLogBtn.setStyle(BTN_PRIMARY);
            addLogBtn.setOnAction(e -> {
                pet.addMedicalRecord(logField.getText());
                saveAllData();
                window.setScene(buildPetProfileScene(pet, user));
            });
            addMedForm.getChildren().addAll(logField, addLogBtn);
            medList.getChildren().add(0, addMedForm);
        }

        content.getChildren().addAll(header, stats, medTitle, medList);
        root.setCenter(new ScrollPane(content));
        return new Scene(root, 900, 750);
    }

    private Scene buildVetDashboard(User user) {
        BorderPane root = new BorderPane();
        root.setStyle(BG_LIGHT);
        root.setTop(buildNavBar(user));

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label welcome = new Label("Veterinarian Dashboard | Dr. " + user.getFullName());
        welcome.setFont(Font.font("System", FontWeight.BOLD, 22));
        welcome.setStyle("-fx-text-fill: #512DA8;");

        HBox split = new HBox(20);

        VBox queueCard = new VBox(15);
        queueCard.setStyle(CARD_STYLE); queueCard.setPadding(new Insets(20)); queueCard.setPrefWidth(350);
        Label qTitle = new Label("📅 Serve Next Patient"); qTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        qTitle.setStyle("-fx-text-fill: #512DA8;");
        TextArea qLog = new TextArea(); qLog.setEditable(false); qLog.setPrefHeight(80);
        Button serveBtn = new Button("Call Next Patient"); serveBtn.setStyle(BTN_PRIMARY);

        TextField dxField = new TextField(); dxField.setPromptText("Diagnosis");
        TextField medField = new TextField(); medField.setPromptText("Medication");
        TextField docFee = new TextField(); docFee.setPromptText("Doctor Fee (₱)");
        Button completeBtn = new Button("Complete & Save"); completeBtn.setStyle(BTN_PRIMARY);

        VBox bfsCard = new VBox(15);
        bfsCard.setStyle(CARD_STYLE); bfsCard.setPadding(new Insets(20)); HBox.setHgrow(bfsCard, Priority.ALWAYS);
        Label bfsTitle = new Label("🩺 BFS Symptom & Fee Estimator"); bfsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        bfsTitle.setStyle("-fx-text-fill: #512DA8;");
        HBox bfsForm = new HBox(10);
        ComboBox<String> symptomBox = new ComboBox<>();
        symptomBox.getItems().addAll("Lethargy", "Bleeding", "Fracture", "Bad Breath");
        Button runBfsBtn = new Button("Run BFS Diagnostics"); runBfsBtn.setStyle(BTN_OUTLINE);
        TextArea bfsLog = new TextArea(); bfsLog.setEditable(false); bfsLog.setPrefHeight(80);

        runBfsBtn.setOnAction(e -> {
            List<String> path = symptomGraph.bfs(symptomBox.getValue());
            StringBuilder sb = new StringBuilder("Path: " + String.join(" -> ", path) + "\nFees:\n");
            for (String node : path) {
                if (serviceFees.containsKey(node)) sb.append(node).append(": ₱").append(serviceFees.get(node)).append("\n");
            }
            bfsLog.setText(sb.toString());
        });
        bfsForm.getChildren().addAll(symptomBox, runBfsBtn);
        bfsCard.getChildren().addAll(bfsTitle, bfsForm, bfsLog);
        split.getChildren().addAll(queueCard, bfsCard);

        VBox triageCard = new VBox(15);
        triageCard.setStyle(CARD_STYLE); triageCard.setPadding(new Insets(20));
        Label triageTitle = new Label("🚨 Triage & Book Patient"); triageTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        triageTitle.setStyle("-fx-text-fill: #512DA8;");

        HBox triageForm1 = new HBox(10);
        TextField tPet = new TextField(); tPet.setPromptText("Pet Name");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Routine", "Urgent", "Surgical");
        typeBox.setValue("Urgent");
        ComboBox<Integer> sevBox = new ComboBox<>();
        sevBox.getItems().addAll(1, 2, 3, 4, 5);
        sevBox.setValue(3);
        sevBox.setPromptText("Severity");

        HBox triageForm2 = new HBox(10);
        DatePicker tDate = new DatePicker(); tDate.setPromptText("Date");
        ComboBox<Integer> slotBox = new ComboBox<>();
        for (int s : clinicTimeSlots) slotBox.getItems().add(s);
        slotBox.setPromptText("Time Slot");
        TextField tReason = new TextField(); tReason.setPromptText("Reason");
        Button tBookBtn = new Button("Add to Queue"); tBookBtn.setStyle(BTN_PRIMARY);

        triageForm1.getChildren().addAll(tPet, typeBox, sevBox);
        triageForm2.getChildren().addAll(tDate, slotBox, tReason, tBookBtn);
        triageCard.getChildren().addAll(triageTitle, triageForm1, triageForm2);

        VBox scheduleCard = new VBox(10);
        scheduleCard.setStyle(CARD_STYLE); scheduleCard.setPadding(new Insets(20));
        Label schedTitle = new Label("📋 Full Appointments Schedule"); schedTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        schedTitle.setStyle("-fx-text-fill: #512DA8;");

        TableView<Appointment> schedTable = new TableView<>();
        TableColumn<Appointment, String> dateCol = new TableColumn<>("Date"); dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Appointment, Integer> timeCol = new TableColumn<>("Time"); timeCol.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        TableColumn<Appointment, String> petCol = new TableColumn<>("Pet Name"); petCol.setCellValueFactory(new PropertyValueFactory<>("petName"));
        TableColumn<Appointment, String> typeCol = new TableColumn<>("Case Type"); typeCol.setCellValueFactory(new PropertyValueFactory<>("caseType"));
        TableColumn<Appointment, Integer> sevCol = new TableColumn<>("Severity"); sevCol.setCellValueFactory(new PropertyValueFactory<>("severityLevel"));
        TableColumn<Appointment, String> statCol = new TableColumn<>("Status"); statCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        schedTable.getColumns().addAll(dateCol, timeCol, petCol, typeCol, sevCol, statCol);
        schedTable.setPrefHeight(250);

        Runnable refreshSchedule = () -> {
            List<Appointment> list = new ArrayList<>();
            for (Appointment a : allAppointments) {
                if (a.getVetName().equals("Dr. " + user.getFullName())) {
                    list.add(a);
                }
            }
            if (!list.isEmpty()) ClinicAlgorithms.quickSortAppointments(list, 0, list.size() - 1);
            schedTable.getItems().setAll(list);
        };
        refreshSchedule.run();

        tBookBtn.setOnAction(e -> {
            if(slotBox.getValue() == null || tDate.getValue() == null) { showAlert("Error", "Select a date and time slot"); return; }

            boolean conflict = false;
            for(Appointment a : allAppointments) {
                if(a.getVetName().equals("Dr. " + user.getFullName()) && a.getDate().equals(tDate.getValue().toString()) && a.getTimeSlot() == slotBox.getValue()) {
                    conflict = true;
                    break;
                }
            }
            if(conflict) {
                showAlert("Error", "Time slot already booked for this veterinarian.");
                return;
            }

            String ownerUser = "";
            for (Pet p : petList.toList()) {
                if (p.getName().equalsIgnoreCase(tPet.getText())) {
                    ownerUser = p.getOwnerUsername();
                    break;
                }
            }

            Appointment apt = new Appointment(ownerUser, tPet.getText(), typeBox.getValue(), sevBox.getValue(), tDate.getValue().toString(), slotBox.getValue(), "Dr. " + user.getFullName(), tReason.getText());
            allAppointments.add(apt);
            saveAllData();
            refreshSchedule.run();
            undoStack.push(() -> {
                allAppointments.remove(apt);
                saveAllData();
                refreshSchedule.run();
            });
            showAlert("Success", "Patient triaged and queued.");
        });

        serveBtn.setOnAction(e -> {
            activePatient = null;
            List<Appointment> pending = new ArrayList<>();
            for(Appointment a : allAppointments) {
                if(a.getVetName().equals("Dr. " + user.getFullName()) && "Pending".equals(a.getStatus())) {
                    pending.add(a);
                }
            }
            if(!pending.isEmpty()) {
                pending.sort((a1, a2) -> {
                    if (a1.getTriageScore() != a2.getTriageScore())
                        return Integer.compare(a2.getTriageScore(), a1.getTriageScore());
                    return Long.compare(a1.getArrivalTimestamp(), a2.getArrivalTimestamp());
                });
                activePatient = pending.get(0);
                qLog.setText("Serving: " + activePatient.getPetName() + "\nTriage Score: " + activePatient.getTriageScore() + "\nReason: " + activePatient.getReason());
            } else {
                qLog.setText("No patients waiting.");
            }
        });

        completeBtn.setOnAction(e -> {
            if(activePatient != null) {
                if(dxField.getText().isEmpty() || docFee.getText().isEmpty()) {
                    showAlert("Error", "Diagnosis and Doctor Fee cannot be empty.");
                    return;
                }
                activePatient.setPrice(Double.parseDouble(docFee.getText()));
                activePatient.setStatus("Completed");

                String logEntry = "Date: " + activePatient.getDate() + " | Vet: " + activePatient.getVetName() + " | Dx: " + dxField.getText() + " | Meds: " + medField.getText() + " | Fee: ₱" + docFee.getText();
                for(Pet p : petList.toList()) {
                    if(p.getName().equalsIgnoreCase(activePatient.getPetName()) && p.getOwnerUsername().equals(activePatient.getOwnerUsername())) {
                        p.addMedicalRecord(logEntry);
                    }
                }
                saveAllData();
                showAlert("Success", "Consultation complete. Details saved to pet's medical record.");
                activePatient = null;
                qLog.setText("");
                dxField.clear(); medField.clear(); docFee.clear();
                refreshSchedule.run();
            } else {
                showAlert("Notice", "No active patient being served.");
            }
        });

        queueCard.getChildren().addAll(qTitle, serveBtn, qLog, dxField, medField, docFee, completeBtn);
        scheduleCard.getChildren().addAll(schedTitle, schedTable);

        HBox searchForm = new HBox(10);
        TextField searchId = new TextField(); searchId.setPromptText("AVL Search by ID");
        Button searchIdBtn = new Button("Find ID"); searchIdBtn.setStyle(BTN_PRIMARY);
        TextField searchName = new TextField(); searchName.setPromptText("Binary Search by Name");
        Button searchNameBtn = new Button("Find Name"); searchNameBtn.setStyle(BTN_PRIMARY);
        Button listAvlBtn = new Button("List All Pets (AVL Inorder)"); listAvlBtn.setStyle(BTN_OUTLINE);
        TextArea searchResultLog = new TextArea(); searchResultLog.setEditable(false); searchResultLog.setPrefHeight(80);

        searchIdBtn.setOnAction(e -> {
            try {
                Pet found = petAvlTree.search(Integer.parseInt(searchId.getText()));
                if (found != null) { recentViewedStack.push(found); window.setScene(buildPetProfileScene(found, user)); }
                else showAlert("Not Found", "Pet not found in AVL Tree.");
            } catch (Exception ex) { showAlert("Error", "Invalid ID."); }
        });

        searchNameBtn.setOnAction(e -> {
            List<Pet> allPets = petList.toList();
            ClinicAlgorithms.quickSortPetsByName(allPets, 0, allPets.size()-1);
            int idx = ClinicAlgorithms.binarySearchPetByName(allPets, searchName.getText());
            if (idx != -1) { recentViewedStack.push(allPets.get(idx)); window.setScene(buildPetProfileScene(allPets.get(idx), user)); }
            else showAlert("Not Found", "Pet name not found via Binary Search.");
        });

        listAvlBtn.setOnAction(e -> {
            List<Pet> inorderList = new ArrayList<>();
            petAvlTree.inOrderTraversal(inorderList);
            StringBuilder sb = new StringBuilder("AVL Inorder Listing:\n");
            for(Pet p : inorderList) sb.append(p.getId()).append(" - ").append(p.getName()).append("\n");
            searchResultLog.setText(sb.toString());
        });

        searchForm.getChildren().addAll(searchId, searchIdBtn, searchName, searchNameBtn, listAvlBtn);

        Label searchLabel = new Label("Search Global Records:");
        searchLabel.setStyle("-fx-text-fill: #512DA8; -fx-font-weight: bold;");
        content.getChildren().addAll(welcome, split, triageCard, scheduleCard, searchLabel, searchForm, searchResultLog);
        root.setCenter(new ScrollPane(content));
        return new Scene(root, 1000, 850);
    }

    private HBox buildNavBar() { return buildNavBar(null); }
    private HBox buildNavBar(User user) {
        HBox nav = new HBox(15);
        nav.setPadding(new Insets(15, 20, 15, 20));
        nav.setStyle("-fx-background-color: #512DA8;");
        nav.setAlignment(Pos.CENTER_LEFT);
        Label brand = new Label("🐾 VetSetGo"); brand.setFont(Font.font("System", FontWeight.BOLD, 20));
        brand.setTextFill(Color.web("#FFC107"));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        nav.getChildren().addAll(brand, spacer);

        if (user == null) {
            Button loginBtn = new Button("Login");
            loginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
            loginBtn.setOnAction(e -> window.setScene(buildLoginScene())); nav.getChildren().add(loginBtn);
        } else {
            Button logoutBtn = new Button("Logout");
            logoutBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #512DA8; -fx-font-weight: bold; -fx-background-radius: 4;");
            logoutBtn.setOnAction(e -> { auth.logout(); window.setScene(buildLoginScene()); }); nav.getChildren().add(logoutBtn);
        }
        return nav;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }

    private void initClinicData() {
        serviceFees.put("General Consultation", 500.0);
        serviceFees.put("Vaccination", 1000.0);
        serviceFees.put("Surgery", 5000.0);
        serviceFees.put("Emergency Stabilization", 2500.0);
        serviceFees.put("Viral Infection", 1500.0);
        serviceFees.put("Dental Cleaning", 2000.0);
        serviceFees.put("X-Ray", 1500.0);
        serviceFees.put("Cast/Splint", 2000.0);

        symptomGraph.addEdge("Lethargy", "Viral Infection");
        symptomGraph.addEdge("Bleeding", "Trauma");
        symptomGraph.addEdge("Trauma", "Emergency Stabilization");
        symptomGraph.addEdge("Trauma", "Surgery");
        symptomGraph.addEdge("Bad Breath", "Dental Cleaning");
        symptomGraph.addEdge("Fracture", "X-Ray");
        symptomGraph.addEdge("X-Ray", "Cast/Splint");
        symptomGraph.addEdge("X-Ray", "Surgery");
    }

    private void loadSavedData() {
        try {
            if (storage.usersFileExists()) auth.loadUsers(storage.loadUsers());
            if (auth.getUsers().isEmpty()) {
                auth.register("vet1", "Bob Miller", Role.VETERINARIAN, "vet123");
                auth.register("vet2", "Sarah Jenkins", Role.VETERINARIAN, "vet123");
                auth.register("vet3", "Mike Davis", Role.VETERINARIAN, "vet123");
                auth.register("owner", "Alice Johnson", Role.PET_OWNER, "owner123");
            }
            petList = storage.loadPets();
            nextPetId = petList.maxId() + 1;
            for (Pet p : petList.toList()) {
                petAvlTree.insert(p);
                speciesTree.addSpeciesAndBreed(p.getSpecies(), "Unknown");
            }
            for (Appointment a : storage.loadAppointments()) {
                allAppointments.add(a);
            }
        } catch (IOException ex) { System.err.println("Load error: " + ex.getMessage()); }
    }

    private void saveAllData() {
        try {
            storage.saveUsers(auth.getUsers());
            storage.savePets(petList);
            storage.saveAppointments(allAppointments);
        } catch (IOException ex) { System.err.println("Save error: " + ex.getMessage()); }
    }

    public static void main(String[] args) { launch(args); }
}