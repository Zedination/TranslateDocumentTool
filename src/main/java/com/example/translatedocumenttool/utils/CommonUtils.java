package com.example.translatedocumenttool.utils;

import com.example.translatedocumenttool.model.GroupCellData;
import com.example.translatedocumenttool.task.TranslateTask;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonUtils {
    public static boolean isJapanese(String text) {
        // Biểu thức chính quy để kiểm tra ký tự tiếng Nhật
        String regex = "[\\p{InHiragana}\\p{InKatakana}\\p{InCJK_Unified_Ideographs}]+";

        // Tạo Pattern
        Pattern pattern = Pattern.compile(regex);

        // Tạo Matcher
        Matcher matcher = pattern.matcher(text);

        // Kiểm tra xem có khớp hay không
        return matcher.find();
    }

    public static void drawBorderRangeCell(Sheet sheet, int startRowIndex, int endRowIndex, int columnIndex) {
        // Chọn nhóm các ô cần đặt đường viền outline
        CellRangeAddress selectedRange = new CellRangeAddress(startRowIndex, endRowIndex, columnIndex, columnIndex);

        // Áp dụng CellStyle cho toàn bộ nhóm
        RegionUtil.setBorderTop(BorderStyle.THICK, selectedRange, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THICK, selectedRange, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THICK, selectedRange, sheet);
        RegionUtil.setBorderRight(BorderStyle.THICK, selectedRange, sheet);

        RegionUtil.setTopBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
        RegionUtil.setBottomBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
        RegionUtil.setLeftBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
        RegionUtil.setRightBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
    }

    public static String translate(String srcText, String srcLang, String targetLang, String transServerEndpoint) {
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

    private static String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String getCachedValueOfCell(Cell cell){
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

    public static void translateCellByCell(Workbook workbook, List<String> sheetsList, String srcLang, String targetLang, String endpoint, long count, AtomicLong progress, TranslateTask task) {
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
                            translationText = CommonUtils.translate(srcText,srcLang, targetLang, endpoint);
                        } catch (Exception e) {
                            progress.addAndGet(1);
                            task.updateProgress(progress.get(), count);
                            continue;
                        }
                        if (Objects.nonNull(translationText)) cell.setCellValue(translationText);
                    }
                    progress.addAndGet(1);
                    task.updateProgress(progress.get(), count);
                }
            }
        });
    }

    public static void translateGroupByCellSameColumn(Workbook workbook, List<String> sheetsList, String srcLang, String targetLang, String endpoint, long count, AtomicLong progress, TranslateTask task) {
        sheetsList.forEach(sheetName -> {
            Sheet sheet = workbook.getSheet(sheetName);
            var rowIter = sheet.rowIterator();
            Map<Integer, GroupCellData> mapCell = new HashMap<>();
            while (rowIter.hasNext()) {
                var row = rowIter.next();
                var cellIter = row.cellIterator();
                while (cellIter.hasNext()) {
                    var cell = cellIter.next();
                    var celVal = getStringValueFromCell(workbook, cell);
                    if (StringUtils.isBlank(celVal)) {
                        progress.addAndGet(1);
                        task.updateProgress(progress.get(), count);
                        continue;
                    }
                    GroupCellData groupCellData = mapCell.getOrDefault(cell.getColumnIndex(), new GroupCellData(row.getRowNum(), new ArrayList<>()));
                    groupCellData.getTargetTexts().add(celVal);
                    mapCell.put(cell.getColumnIndex(), groupCellData);
                    var bellowCellVal = Optional.ofNullable(sheet.getRow(row.getRowNum() + 1)).map(r -> r.getCell(cell.getColumnIndex()))
                            .map(c -> getStringValueFromCell(workbook, c)).orElse(StringUtils.EMPTY);

                    if (StringUtils.isBlank(bellowCellVal)) {
                        // start translate
                        String output = groupCellData.processText().stream().map(s -> CommonUtils.translate(s, srcLang, targetLang, endpoint))
                                .collect(Collectors.joining("\n"));
                        addComment(workbook, sheet, sheet.getRow(groupCellData.getFirstDataIndex()), sheet.getRow(groupCellData.getFirstDataIndex()).getCell(cell.getColumnIndex()), "Tool dịch AI" ,output);
                        CommonUtils.drawBorderRangeCell(sheet, groupCellData.getFirstDataIndex(), row.getRowNum(), cell.getColumnIndex());
                        mapCell.remove(cell.getColumnIndex());
                    }
                    progress.addAndGet(1);
                    task.updateProgress(progress.get(), count);
                }
            }
        });
    }



    public static void addComment(Workbook workbook, Sheet sheet, Row row, Cell cell, String author, String commentText) {
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

    public static String getStringValueFromCell(Workbook workbook, Cell cell) {
        DataFormatter dataFormatter = new DataFormatter();
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        try {
            return dataFormatter.formatCellValue(cell, formulaEvaluator);
        }catch (Exception e){
            return CommonUtils.getCachedValueOfCell(cell);
        }
    }

    public static boolean isValidURL(String urlString) {
        try {
            new URL(urlString);
            // Nếu không có ngoại lệ nào được ném, URL là hợp lệ
            return true;
        } catch (MalformedURLException e) {
            // Nếu có ngoại lệ MalformedURLException, URL không hợp lệ
            return false;
        }
    }
}
