package com.example.meetmindai;

public class Meeting {
    private String id; // Tambahkan field ID untuk Firebase
    private String title;
    private String date;
    private String startTime;
    private String endTime;
    private String location;
    private String category;
    private String notes;
    private boolean useAi;
    private boolean useReminder;
    private String status;

    public Meeting() {
    }

    public Meeting(String id, String title, String date, String startTime, String endTime, String location, String category, String notes, boolean useAi, boolean useReminder, String status) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.category = category;
        this.notes = notes;
        this.useAi = useAi;
        this.useReminder = useReminder;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getNotes() { return notes; }
    public boolean isUseAi() { return useAi; }
    public boolean isUseReminder() { return useReminder; }
    public String getStatus() { return status; }
    
    public String getTime() { return startTime; }
    public String getParticipants() { return "0"; }
}
