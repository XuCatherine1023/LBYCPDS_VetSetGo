module ph.edu.dlsu.lbycpds.lbycpds_vetsetgodemo {
    requires javafx.controls;
    requires javafx.fxml;

    opens ph.edu.dlsu.lbycpds.lbycpds_vetsetgo to javafx.fxml, javafx.graphics;
    exports ph.edu.dlsu.lbycpds.lbycpds_vetsetgo;
}
