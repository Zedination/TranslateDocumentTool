package com.example.translatedocumenttool;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
//        root.getStyleClass().add(JMetroStyleClass.BACKGROUND);
//        JMetro jMetro = new JMetro(Style.DARK);
//        JMetro jMetro = new JMetro(Style.LIGHT);
        Scene scene = new Scene(root);
//        jMetro.setScene(scene);
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        stage.setTitle("Tool dịch Excel AI!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}