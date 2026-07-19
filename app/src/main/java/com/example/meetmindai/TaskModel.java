package com.example.meetmindai;

import com.google.firebase.database.Exclude;

public class TaskModel {
    private String id; // Field untuk menampung key Firebase
    private String title;
    private String meetingSource;
    private String pic;
    private String deadline;
    private String priority; // "High", "Medium", "Low"
    private String status;   // "To Do", "In Progress", "Completed"
    private int progress;    // 0-100

    // Required empty constructor for Firebase
    public TaskModel() {
    }

    public TaskModel(String title, String meetingSource, String pic, String deadline,
                     String priority, String status, int progress) {
        this.title = title;
        this.meetingSource = meetingSource;
        this.pic = pic;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.progress = progress;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMeetingSource() { return meetingSource; }
    public void setMeetingSource(String meetingSource) { this.meetingSource = meetingSource; }

    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}
