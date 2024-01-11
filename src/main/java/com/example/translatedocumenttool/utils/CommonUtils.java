package com.example.translatedocumenttool.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
