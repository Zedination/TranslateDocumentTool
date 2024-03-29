package com.example.translatedocumenttool;

import atlantafx.base.controls.Notification;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import atlantafx.base.util.BBCodeParser;
import com.example.translatedocumenttool.component.AutoCompleteTextField;
import com.example.translatedocumenttool.constant.AppConstant;
import com.example.translatedocumenttool.constant.NotificationType;
import com.example.translatedocumenttool.task.Functional;
import com.example.translatedocumenttool.task.TranslateTask;
import com.example.translatedocumenttool.utils.CommonUtils;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.controlsfx.control.CheckComboBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

public class MainController {

    @FXML
    private TextField selectFileInput;

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
    private ToggleSwitch switchModeToggle;

    @FXML
    private ProgressBar progressTranslateBar;

    @FXML
    private ComboBox<String> engineCombobox;

    private Service<Void> translateService;

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

        translateService = new Service<>() {

            String fileName;
            TranslateTask translateTask;

            @Override
            protected void succeeded() {
                super.succeeded();
                Path partPath = Paths.get(fileName);
                showNotification("Dịch file hoàn thành, output: " + partPath.toAbsolutePath(), NotificationType.SUCCESS, () -> {
                    String command = """
                            explorer "%s"
                            """.formatted(partPath.toAbsolutePath());
                    try {
                        Runtime.getRuntime().exec(command);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                progressTranslateBar.progressProperty().unbind();

            }

            @Override
            protected void cancelled() {
                super.cancelled();
                showNotification("Quá trình dịch đã bị dừng lại!", NotificationType.INFO, null);
                File file = new File(fileName);
                file.delete();
                progressTranslateBar.progressProperty().unbind();
                progressTranslateBar.progressProperty().set(0);
            }

            @Override
            protected void failed() {
                super.failed();
                showNotification("Có lỗi xảy ra trong quá trình xử lý", NotificationType.ERROR, null);
                progressTranslateBar.progressProperty().unbind();
                progressTranslateBar.progressProperty().set(0);
            }

            @Override
            public void start() {
                super.start();
            }

            @Override
            protected Task<Void> createTask() {
                translateTask = new TranslateTask(() -> {
                    long count = 0;
                    AtomicLong progress = new AtomicLong(0);

                    var srcFile = new File(selectFileInput.getText());
                    String srcLang = sourceLangInput.getText().split(" - ")[sourceLangInput.getText().split(" - ").length - 1];
                    String targetLang = targetLangInput.getText().split(" - ")[targetLangInput.getText().split(" - ").length - 1];

                    fileName = MessageFormat.format("{0}/{1}_{2}_{3}.xlsx", srcFile.getParent(), srcFile.getName().replace(".xlsx", ""),
                            DateTimeFormatter.ofPattern("yyyyMMdd hhmmss").format(LocalDateTime.now()), targetLang);
                    // start logic translate
                    List<String> sheetsNames = new ArrayList<>(sheetComboBox.getCheckModel().getCheckedItems().stream().toList());
                    if (sheetsNames.contains(sheetComboBox.getItems().get(0))) {
                        sheetsNames = sheetComboBox.getItems().stream().filter(s -> !s.equals(sheetComboBox.getItems().get(0))).toList();
                    }
                    try (FileInputStream fis = new FileInputStream(selectFileInput.getText());
                         Workbook workbook = WorkbookFactory.create(fis);
                         FileOutputStream fos = new FileOutputStream(fileName);){
                        count = calculateTotalProcess(sheetsNames, workbook);
                        if (switchModeToggle.isSelected()) {
                            CommonUtils.translateGroupByCellSameColumn(workbook, sheetsNames, StringUtils.trim(srcLang),
                                    StringUtils.trim(targetLang) ,StringUtils.trim(endpointInput.getText()), count, progress, translateTask);
                        } else {
                            CommonUtils.translateCellByCell(workbook, sheetsNames, StringUtils.trim(srcLang),
                                    StringUtils.trim(targetLang) ,StringUtils.trim(endpointInput.getText()), count, progress, translateTask);
                        }
                        workbook.write(fos);
                    } catch (FileNotFoundException e) {
                        alertFail("File không tồn tại!");
                    } catch (IOException e) {
                        alertFail("Có lỗi xảy ra: " + e.getMessage());
                    }
                });
                return translateTask;
            }
        };
        this.progressTranslateBar.progressProperty().bind(translateService.progressProperty());
        translateService.start();
    }

    @FXML
    protected void onCancelButtonClick() {
        if (Objects.nonNull(this.translateService)) {
            this.translateService.cancel();
        }
    }

    private long calculateTotalProcess(List<String> sheets, Workbook workbook) {
        AtomicLong counter = new AtomicLong(0);
        sheets.forEach(sheetName -> {
            Sheet sheet = workbook.getSheet(sheetName);
            var rowIter = sheet.rowIterator();
            while (rowIter.hasNext()) {
                var row = rowIter.next();
                Iterable<Cell> newIterable = row::cellIterator;
                counter.addAndGet(StreamSupport.stream(newIterable.spliterator(), false).count());
            }
        });
        return counter.get();
    }

    @FXML
    protected void swapLangInput() {
        String temp = this.sourceLangInput.getText();
        this.sourceLangInput.setText(this.targetLangInput.getText());
        this.targetLangInput.setText(temp);
        this.sourceLangInput.getEntriesPopup().hide();
        this.targetLangInput.getEntriesPopup().hide();
    }

    @FXML
    protected void showAuthorDialog() {
        this.alertAuthor();
    }

    @FXML
    protected void closeApp() {
//        System.exit(0);
        Platform.exit();
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

        this.loadListTranslateEngine();
        this.loadListLang();

        this.progressTranslateBar.getStyleClass().add(Styles.SMALL);
    }

    @FXML
    protected void loadListLang() {
        List<String> langList = new ArrayList<>(List.of(StringUtils.trim(AppConstant.MAP_LANGS.get(StringUtils.trim(this.engineCombobox.getValue()))).split("\n")));
        this.sourceLangInput.getEntries().clear();
        this.targetLangInput.getEntries().clear();
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
            alertException("File không tồn tại!", e);
        } catch (IOException e) {
            alertException("IO Exception", e);
        }
    }

    private void loadListTranslateEngine() {
        this.engineCombobox.getItems().addAll("MADLAD-400-MT", "NLLB-200");
        this.engineCombobox.getSelectionModel().selectFirst();
    }


    private void alertFail(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi!");
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.show();
    }

    private void alertAuthor() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Translate Tool AI");
        String authorInfo = """
                Tác giả: Lê Anh Đức
                Email: leanhduc9999@gmail.com
                Website: [url]https://zedination.dev[/url]
                
                [b]NLLB-200:[/b] Model được training bởi Meta AI, hỗ trợ hơn 200 ngôn ngữ, có chất lượng dịch tương đối tốt,
                tốc độ dịch nhanh hơn MADLAD-400;
                License: CC-BY-NC-4.0
                
                [b]MADLAD-400-MT:[/b] Model được traning bởi Google, hỗ trợ 419 ngôn ngữ, có chất lượng dịch rất tốt
                nhưng tốc độ dịch chậm hơn NLLB-200;
                License: Apache 2.0
                
                Chi tiết tài liệu xem tại [url]https://arxiv.org/pdf/2309.04662.pdf[/url]
                """;
        alert.setHeaderText(null);
        VBox vBox = BBCodeParser.createLayout(authorInfo);
        vBox.setMaxWidth(550);
        alert.setGraphic(vBox);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.show();
    }

    private void alertException(String text, Exception e) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Có lỗi xảy ra, chi tiết vui lòng xem lại stacktrace");
        alert.setContentText(text);

        var stringWriter = new StringWriter();
        var printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);

        var textArea = new TextArea(stringWriter.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        var content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(new Label("Full stacktrace:"), 0, 0);
        content.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.show();
    }

    private boolean validate() {
        // clear old validate
        this.selectFileInput.pseudoClassStateChanged(Styles.STATE_DANGER, false);
        this.sheetComboBox.setBorder(null);
        this.sourceLangInput.pseudoClassStateChanged(Styles.STATE_DANGER, false);
        this.targetLangInput.pseudoClassStateChanged(Styles.STATE_DANGER, false);
        this.endpointInput.pseudoClassStateChanged(Styles.STATE_DANGER, false);

        boolean isValid = true;
        if (StringUtils.isBlank(this.selectFileInput.getText())) {
            this.selectFileInput.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            isValid = false;
        }
        if(this.sheetComboBox.getCheckModel().getCheckedItems().isEmpty()) {
            Border redBorder = new Border(
                    new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1)));
            this.sheetComboBox.setBorder(redBorder);
            isValid = false;
        }
        if (!this.sourceLangInput.getEntries().contains(this.sourceLangInput.getText()) && !StringUtils.isBlank(this.sourceLangInput.getText())) {
            this.sourceLangInput.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            isValid = false;
        }
        if (StringUtils.isBlank(this.targetLangInput.getText()) || !this.targetLangInput.getEntries().contains(this.targetLangInput.getText())) {
            this.targetLangInput.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            isValid = false;
        }
        if (!CommonUtils.isValidURL(this.endpointInput.getText())) {
            this.endpointInput.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            isValid = false;
        }
        if (!isValid) this.showNotification("Vui lòng kiểm tra lại! Các trường được bôi đỏ bắt buộc phải nhập đúng định dạng.", NotificationType.ERROR, null);
        return isValid;
    }

    private void showNotification(String text, NotificationType notificationType, Functional functional) {
        Notification msg = null;
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
        Notification finalMsg1 = msg;
        msg.setOnClose(e -> {
            finalMsg1.setOnMouseClicked(f -> {});
            var out = Animations.slideOutUp(finalMsg, Duration.millis(250));
            out.setOnFinished(f -> this.stackPanelNotification.getChildren().remove(finalMsg));
            out.playFromStart();
        });
        if (Objects.nonNull(functional)) {
            msg.setOnMouseClicked(e -> functional.execute());
        }
        if (!this.stackPanelNotification.getChildren().contains(msg)) {
            this.stackPanelNotification.getChildren().add(msg);
        }
        in.playFromStart();
    }
}