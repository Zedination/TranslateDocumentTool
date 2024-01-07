module com.example.translatedocumenttool {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.jfxtras.styles.jmetro;
    requires com.google.gson;
    requires org.apache.poi.poi;

    opens com.example.translatedocumenttool to javafx.fxml;
    exports com.example.translatedocumenttool;
}