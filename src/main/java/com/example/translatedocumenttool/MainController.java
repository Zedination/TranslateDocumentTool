package com.example.translatedocumenttool;

import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private TextField selectFileInput;

    @FXML
    private Button selectFileButton;

    @FXML
    private CheckComboBox<String> sheetComboBox;

//    private static boolean isSelectAll = false;


    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onSelectFileButtonClick(Event event) {
        Node node = (Node) event.getSource();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Bcomp cần mở");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel xlsx file", "*.xlsx"));
        File file = fileChooser.showOpenDialog(node.getScene().getWindow());
        if (Objects.nonNull(file)) {
            this.selectFileInput.setText(file.getAbsolutePath());
            // load list sheets in excel file
            this.loadListSheetsForCombobox(file);

        }
    }

    @FXML
    public void initialize() {
        this.sheetComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<>() {
            private boolean changing = false;

            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                if (!changing && sheetComboBox.getCheckModel().isChecked(0)) {
                    // trigger no more calls to checkAll when the selected items are modified by checkAll
                    changing = true;
                    sheetComboBox.getCheckModel().checkAll();
                    changing = false;
                }
            }
        });

    }

    private void loadListSheetsForCombobox(File file) {
        this.sheetComboBox.getItems().clear();
        this.sheetComboBox.getItems().add("Chọn tất cả");
        try (FileInputStream fis = new FileInputStream(file)){
            try (Workbook workbook = WorkbookFactory.create(fis)) {
                var sheetIter = workbook.sheetIterator();
                while (sheetIter.hasNext()) {
                    sheetComboBox.getItems().add(sheetIter.next().getSheetName());
                }
            } catch (Exception ex) {
                alertFail("Có lỗi xảy ra!" + ex.getMessage());
            }
        } catch (IOException e) {
            alertFail("Có lỗi xảy ra!" + e.getMessage());
        }
    }

    private void alertFail(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi!");
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add(JMetroStyleClass.BACKGROUND);
        alert.setHeaderText("Có lỗi xảy ra!");
        alert.initModality(Modality.APPLICATION_MODAL);
        new JMetro(Style.DARK).setParent(alert.getDialogPane());
        alert.show();
    }
}