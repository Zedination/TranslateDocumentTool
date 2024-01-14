module com.example.translatedocumenttool {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
//    requires org.jfxtras.styles.jmetro;
    requires com.google.gson;
    requires org.apache.poi.poi;
    requires atlantafx.base;
//    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material2;
    requires org.apache.commons.lang3;
    requires java.net.http;
    requires java.desktop;

    opens com.example.translatedocumenttool to javafx.fxml;
    opens com.example.translatedocumenttool.component to javafx.fxml;
    exports com.example.translatedocumenttool;
}