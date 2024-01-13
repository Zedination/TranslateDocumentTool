package com.example.translatedocumenttool.task;

import javafx.concurrent.Task;

public class TranslateTask extends Task<Void> {
    Functional functional;
    public TranslateTask(Functional functional) {
        this.functional = functional;
    }

    @Override
    protected Void call() {
        this.functional.execute();
        return null;
    }

    @Override
    public void updateProgress(long l, long l1) {
        super.updateProgress(l, l1);
    }
}
