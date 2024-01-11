package com.example.translatedocumenttool;

import atlantafx.base.controls.Notification;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import com.example.translatedocumenttool.component.AutoCompleteTextField;
import com.example.translatedocumenttool.constant.NotificationType;
import com.example.translatedocumenttool.utils.CommonUtils;
import impl.org.controlsfx.ImplUtils;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.StringUtil;
import org.controlsfx.control.CheckComboBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainController {

    @FXML
    private TextField selectFileInput;

    @FXML
    private Button selectFileButton;

    @FXML
    private Button buttonSwitch;

    @FXML
    private StackPane stackPanelNotification;

    @FXML
    private AutoCompleteTextField sourceLangInput;

    @FXML
    private AutoCompleteTextField targetLangInput;

    @FXML
    private TextField endpointInput;

    @FXML
    private CheckComboBox<String> sheetComboBox;

    @FXML
    private Button translateButton;

    @FXML
    private ToggleSwitch switchModeToggle;

    @FXML
    protected void onSelectFileButtonClick(Event event) {
        Node node = (Node) event.getSource();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel cần dịch");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel xlsx file", "*.xlsx"));
        File file = fileChooser.showOpenDialog(node.getScene().getWindow());
        if (Objects.nonNull(file)) {
            this.selectFileInput.setText(file.getAbsolutePath());
            // load list sheets in excel file
            this.loadListSheetsForCombobox(file);
        }
    }

    @FXML
    protected void onTranslateButtonClick() {
        if (!this.validate()) return;
        var srcFile = new File(this.selectFileInput.getText());
        String srcLang = this.sourceLangInput.getText().split(" - ")[0];
        String targetLang = this.targetLangInput.getText().split(" - ")[0];

        String fileName = srcFile.getParent() + "/" + srcFile.getName().replace(".xlsx", "") + System.currentTimeMillis() +"_"+targetLang+ ".xlsx";
        // start logic translate
        List<String> sheetsNames = new ArrayList<>(this.sheetComboBox.getCheckModel().getCheckedItems().stream().toList());
        if (sheetsNames.contains(this.sheetComboBox.getItems().get(0))) sheetsNames = this.sheetComboBox.getItems();
        try (FileInputStream fis = new FileInputStream(this.selectFileInput.getText());
             Workbook workbook = WorkbookFactory.create(fis);
             FileOutputStream fos = new FileOutputStream(fileName);){
            if (this.switchModeToggle.isSelected()) {
                CommonUtils.translateGroupByCellSameColumn(workbook, sheetsNames, StringUtils.trim(srcLang),
                        StringUtils.trim(targetLang) ,StringUtils.trim(this.endpointInput.getText()));
            } else {
                CommonUtils.translateCellByCell(workbook, sheetsNames, StringUtils.trim(srcLang),
                        StringUtils.trim(targetLang) ,StringUtils.trim(this.endpointInput.getText()));
            }
            workbook.write(fos);
        } catch (FileNotFoundException e) {
            alertFail("File không tồn tại!");
        } catch (IOException e) {
            alertFail("Có lỗi xảy ra: " + e.getMessage());
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

        List<String> langList = new ArrayList<>(List.of(StringUtils.trim(CommonUtils.LIST_LANG).split("/n")));
        this.sourceLangInput.getEntries().addAll(langList);
        this.targetLangInput.getEntries().addAll(langList);
    }

    private void loadListSheetsForCombobox(File file) {
        this.sheetComboBox.getItems().clear();
        this.sheetComboBox.getItems().add("Chọn tất cả");
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = WorkbookFactory.create(fis)) {
            var sheetIter = workbook.sheetIterator();
            while (sheetIter.hasNext()) {
                sheetComboBox.getItems().add(sheetIter.next().getSheetName());
            }
        } catch (FileNotFoundException e) {
            alertFail("File không tồn tại!");
        } catch (IOException e) {
            alertFail("Có lỗi xảy ra: " + e.toString());
        }
    }


    private void alertFail(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi!");
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.show();
    }

    private boolean validate() {
        // clear old validate
        this.selectFileInput.pseudoClassStateChanged(Styles.STATE_DANGER, false);
//        this.sheetComboBox.getStylesheets().remove(CSS);
        this.sheetComboBox.setBorder(null);
        this.targetLangInput.pseudoClassStateChanged(Styles.STATE_DANGER, false);
        this.endpointInput.pseudoClassStateChanged(Styles.STATE_DANGER, false);

        boolean isValid = true;
        if (StringUtils.isBlank(this.selectFileInput.getText())) {
            this.selectFileInput.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            isValid = false;
        }
        if(this.sheetComboBox.getCheckModel().getCheckedItems().isEmpty()) {
//            this.sheetComboBox.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            Border redBorder = new Border(
                    new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1)));
            this.sheetComboBox.setBorder(redBorder);
            isValid = false;
        }
        if (StringUtils.isBlank(this.targetLangInput.getText())) {
            this.targetLangInput.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            isValid = false;
        }
        if (!CommonUtils.isValidURL(this.endpointInput.getText())) {
            this.endpointInput.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            isValid = false;
        }
        if (!isValid) this.showNotification("Vui lòng kiểm tra lại! Các trường được bôi đỏ bắt buộc phải nhập đúng định dạng.", NotificationType.ERROR);
        return isValid;
    }

    private void showNotification(String text, NotificationType notificationType) {
        Notification msg = null;
        Ikon ikon = null;
        switch (notificationType) {
            case SUCCESS -> {
                msg = new Notification(
                        text,
                        new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE)
                );
                msg.getStyleClass().addAll(
                        Styles.SUCCESS, Styles.ELEVATED_1
                );
            }
            case ERROR -> {
                msg = new Notification(
                        text,
                        new FontIcon(Material2OutlinedAL.ERROR_OUTLINE)
                );
                msg.getStyleClass().addAll(
                        Styles.DANGER, Styles.ELEVATED_1
                );
            }
            default -> {
                msg = new Notification(
                        text,
                        new FontIcon(Material2OutlinedAL.INFO)
                );
                msg.getStyleClass().addAll(
                        Styles.ACCENT, Styles.ELEVATED_1
                );
            }
        }
        msg.setPrefHeight(Region.USE_PREF_SIZE);
        msg.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(msg, Pos.TOP_RIGHT);
        StackPane.setMargin(msg, new Insets(10, 10, 0, 0));

        var in = Animations.slideInDown(msg, Duration.millis(250));

        Notification finalMsg = msg;
        msg.setOnClose(e -> {
            var out = Animations.slideOutUp(finalMsg, Duration.millis(250));
            out.setOnFinished(f -> this.stackPanelNotification.getChildren().remove(finalMsg));
            out.playFromStart();
        });
        if (!this.stackPanelNotification.getChildren().contains(msg)) {
            this.stackPanelNotification.getChildren().add(msg);
        }
        in.playFromStart();
    }
}