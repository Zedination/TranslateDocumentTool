package com.example.translatedocumenttool.model;

import com.example.translatedocumenttool.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupCellData {

    private int firstDataIndex;

    private List<String> targetTexts;

    public GroupCellData(int firstDataIndex, List<String> targetTexts) {
        this.firstDataIndex = firstDataIndex;
        this.targetTexts = targetTexts;
    }

    public int getFirstDataIndex() {
        return firstDataIndex;
    }

    public void setFirstDataIndex(int firstDataIndex) {
        this.firstDataIndex = firstDataIndex;
    }

    public List<String> getTargetTexts() {
        return targetTexts;
    }

    public void setTargetTexts(List<String> targetTexts) {
        this.targetTexts = targetTexts;
    }

    public List<String> processText() {
        String text = String.join("\n", this.targetTexts);
        if (CommonUtils.isJapanese(text)) {
            return new ArrayList<>(List.of(text.split("(?<=。)")));
        }
        return new ArrayList<>(List.of(text.split("(?<=\\.)")));
    }
}
