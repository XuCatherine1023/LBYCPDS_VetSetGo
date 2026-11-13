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

