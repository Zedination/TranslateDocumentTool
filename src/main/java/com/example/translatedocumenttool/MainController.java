package com.example.translatedocumenttool;

import atlantafx.base.controls.ToggleSwitch;
import com.example.translatedocumenttool.component.AutoCompleteTextField;
import com.example.translatedocumenttool.model.GroupCellData;
import com.example.translatedocumenttool.utils.CommonUtils;
import com.google.gson.JsonParser;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.controlsfx.control.CheckComboBox;

import java.io.*;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private TextField selectFileInput;

    @FXML
    private Button selectFileButton;

    @FXML
    private Button buttonSwitch;

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
    protected void onTranslateButtonClick() {
        // start logic translate
        List<String> sheetsNames = new ArrayList<>(this.sheetComboBox.getItems().stream().toList());
        sheetsNames.remove(0);
        try (FileInputStream fis = new FileInputStream(this.selectFileInput.getText());
             Workbook workbook = WorkbookFactory.create(fis);
             FileOutputStream fos = new FileOutputStream(new File(this.selectFileInput.getText()).getParent() + "/test-translate-file.xlsx");){
            if (this.switchModeToggle.isSelected()) {
                translateGroupByCellSameColumn(workbook, sheetsNames, StringUtils.trim(this.sourceLangInput.getText()),
                        StringUtils.trim(this.targetLangInput.getText()) ,StringUtils.trim(this.endpointInput.getText()));
            } else {
                translateCellByCell(workbook, sheetsNames, StringUtils.trim(this.sourceLangInput.getText()),
                        StringUtils.trim(this.targetLangInput.getText()) ,StringUtils.trim(this.endpointInput.getText()));
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

    private void translateCellByCell(Workbook workbook, List<String> sheetsList, String srcLang, String targetLang, String endpoint) {
        sheetsList.forEach(sheetName -> {
            Sheet sheet = workbook.getSheet(sheetName);
            var rowIter = sheet.rowIterator();
            while (rowIter.hasNext()) {
                var row = rowIter.next();
                var cellIter = row.cellIterator();
                while (cellIter.hasNext()) {
                    var cell = cellIter.next();
                    if (CellType.STRING.equals(cell.getCellType()) || CellType.FORMULA.equals(cell.getCellType())) {
                        String srcText = getStringValueFromCell(workbook, cell);
                        if (StringUtils.trimToEmpty(srcText).equals(StringUtils.EMPTY)) continue;
                        String translationText = null;
                        try {
                            translationText = translate(srcText,srcLang, targetLang, endpoint);
                        } catch (Exception e) {
                            continue;
                        }
                        if (Objects.nonNull(translationText)) cell.setCellValue(translationText);
                    }
                }
            }
        });
    }

    private void translateGroupByCellSameColumn(Workbook workbook, List<String> sheetsList, String srcLang, String targetLang, String endpoint) {
        sheetsList.forEach(sheetName -> {
            Sheet sheet = workbook.getSheet(sheetName);
            var rowIter = sheet.rowIterator();
            Map<Integer, GroupCellData> mapCell = new HashMap<>();
            while (rowIter.hasNext()) {
                var row = rowIter.next();
                var cellIter = row.cellIterator();
                while (cellIter.hasNext()) {
                    var cell = cellIter.next();
                    var celVal = this.getStringValueFromCell(workbook, cell);
                    if (StringUtils.isBlank(celVal)) {
                        continue;
                    }
                    GroupCellData groupCellData = mapCell.getOrDefault(cell.getColumnIndex(), new GroupCellData(row.getRowNum(), new ArrayList<>()));
                    groupCellData.getTargetTexts().add(celVal);
                    mapCell.put(cell.getColumnIndex(), groupCellData);
                    var bellowCellVal = Optional.ofNullable(sheet.getRow(row.getRowNum() + 1)).map(r -> r.getCell(cell.getColumnIndex()))
                            .map(c -> this.getStringValueFromCell(workbook, c)).orElse(StringUtils.EMPTY);

                    if (StringUtils.isBlank(bellowCellVal)) {
                        // start translate
                        String output = groupCellData.processText().stream().map(s -> this.translate(s, srcLang, targetLang, endpoint))
                                .collect(Collectors.joining("\n"));
                        this.addComment(workbook, sheet, sheet.getRow(groupCellData.getFirstDataIndex()), sheet.getRow(groupCellData.getFirstDataIndex()).getCell(cell.getColumnIndex()), "Tool dịch AI" ,output);
                        CommonUtils.drawBorderRangeCell(sheet, groupCellData.getFirstDataIndex(), row.getRowNum(), cell.getColumnIndex());
                        mapCell.remove(cell.getColumnIndex());
                    }
                }
            }
        });
    }

    private String translate(String srcText, String srcLang, String targetLang, String transServerEndpoint) {
        var param = "?src_lang=" + encodeValue(srcLang) + "&src_text=" + encodeValue(srcText) + "&target_lang=" +  encodeValue(targetLang);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(transServerEndpoint + param))
                .GET()
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return StringUtils.EMPTY;
        }
        if (response.statusCode() == 200) {
            return JsonParser.parseString(response.body()).getAsJsonObject().get("target_text").getAsString();
        } else {
            return StringUtils.EMPTY;
        }
    }

    private void addCommentToCell(Workbook workbook, Sheet sheet, Row row, Cell cell, String content) {
        // Tạo Drawing và thêm Comment
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper creationHelper = workbook.getCreationHelper();
        ClientAnchor anchor = creationHelper.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setRow1(row.getRowNum());

        // Tạo Comment
        Comment comment = drawing.createCellComment(anchor);
        RichTextString commentText = creationHelper.createRichTextString(content);
        comment.setString(commentText);

        cell.setCellComment(comment);
    }

    private void addComment(Workbook workbook, Sheet sheet, Row row, Cell cell, String author, String commentText) {
        CreationHelper factory = workbook.getCreationHelper();
        //get an existing cell or create it otherwise:

        ClientAnchor anchor = factory.createClientAnchor();
        //i found it useful to show the comment box at the bottom right corner
        anchor.setCol1(cell.getColumnIndex() + 1); //the box of the comment starts at this given column...
        anchor.setCol2(cell.getColumnIndex() + 8); //...and ends at that given column
        anchor.setRow1(row.getRowNum() + 1); //one row below the cell...
        anchor.setRow2(row.getRowNum() + 12); //...and 15 rows high

        Drawing drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        //set the comment text and author
        comment.setString(factory.createRichTextString(commentText));
        comment.setAuthor(author);

        cell.setCellComment(comment);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String getStringValueFromCell(Workbook workbook, Cell cell) {
        DataFormatter dataFormatter = new DataFormatter();
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        try {
            return dataFormatter.formatCellValue(cell, formulaEvaluator);
        }catch (Exception e){
            return getCachedValueOfCell(cell);
        }
    }

    private String getCachedValueOfCell(Cell cell){
        DataFormatter formatter = new DataFormatter();
        if (cell.getCellType() == CellType.FORMULA) {
            switch (cell.getCachedFormulaResultType()) {
                case BOOLEAN -> {
                    return String.valueOf(cell.getBooleanCellValue());
                }
                case NUMERIC -> {
                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.HALF_UP);
                    return String.valueOf(df.format(cell.getNumericCellValue()));
                }
                case STRING -> {
                    return cell.getStringCellValue();
                }
            }
        }
        return formatter.formatCellValue(cell);

    }

    private void alertFail(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi!");
        alert.setContentText(message);
        alert.setHeaderText("Có lỗi xảy ra!");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.show();
    }
}