package com.zenith.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLog {

    private static final ActivityLog INSTANCE = new ActivityLog();
    private final ObservableList<String> entries = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ActivityLog() {}

    public static ActivityLog getInstance() { return INSTANCE; }

    public void log(String action) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + action;
        entries.add(0, entry); // newest first
        if (entries.size() > 200) {
            entries.remove(entries.size() - 1);
        }
        System.out.println("LOG: " + entry);
    }

    public ObservableList<String> getEntries() { return entries; }

    public void clear() {
        entries.clear();
        log("Activity log cleared");
    }
}
