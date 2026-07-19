package com.example.meetmindai;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskModel> taskList;

    public TaskAdapter(List<TaskModel> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvMeeting, tvDeadline, tvPriority, tvStatus;
        private final ProgressBar progressBar;
        private final Button btnDetail;
        private final ImageButton btnDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvMeeting = itemView.findViewById(R.id.tvTaskMeeting);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvPriority = itemView.findViewById(R.id.tvPriorityBadge);
            tvStatus = itemView.findViewById(R.id.tvStatusBadge);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnDetail = itemView.findViewById(R.id.btnDetailTask);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
        }

        void bind(TaskModel task) {
            tvTitle.setText(task.getTitle());
            tvMeeting.setText("Dari: " + task.getMeetingSource());
            tvDeadline.setText("Deadline: " + task.getDeadline());
            
            // Translate to Indonesian
            String priority = task.getPriority();
            String priorityDisplay = "N/A";
            if ("High".equals(priority)) priorityDisplay = "Tinggi";
            else if ("Medium".equals(priority)) priorityDisplay = "Sedang";
            else if ("Low".equals(priority)) priorityDisplay = "Rendah";
            tvPriority.setText(priorityDisplay);

            String status = task.getStatus();
            String statusDisplay = "N/A";
            if ("To Do".equals(status)) statusDisplay = "Belum";
            else if ("In Progress".equals(status)) statusDisplay = "Proses";
            else if ("Completed".equals(status)) statusDisplay = "Selesai";
            tvStatus.setText(statusDisplay);
            
            progressBar.setProgress(task.getProgress());

            // Priority badge styling (Professional muted tones)
            GradientDrawable priorityBg = new GradientDrawable();
            priorityBg.setShape(GradientDrawable.RECTANGLE);
            priorityBg.setCornerRadius(16);
            int color;
            switch (priority != null ? priority : "") {
                case "High": color = Color.parseColor("#E53935"); break; // Red
                case "Medium": color = Color.parseColor("#FB8C00"); break; // Orange
                case "Low": color = Color.parseColor("#1E88E5"); break; // Blue
                default: color = Color.parseColor("#757575"); break; // Grey
            }
            priorityBg.setColor(color);
            tvPriority.setBackground(priorityBg);
            tvPriority.setTextColor(Color.WHITE);

            // Status badge styling (Professional muted tones)
            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setShape(GradientDrawable.RECTANGLE);
            statusBg.setCornerRadius(16);
            int statusColor;
            switch (status != null ? status : "") {
                case "To Do": statusColor = Color.parseColor("#546E7A"); break; // Blue Grey
                case "In Progress": statusColor = Color.parseColor("#FBC02D"); break; // Amber
                case "Completed": statusColor = Color.parseColor("#43A047"); break; // Green
                default: statusColor = Color.parseColor("#757575"); break; // Grey
            }
            statusBg.setColor(statusColor);
            tvStatus.setBackground(statusBg);
            tvStatus.setTextColor(Color.WHITE);

            tvStatus.setOnClickListener(v -> {
                String[] options = {"Belum", "Proses", "Selesai"};
                String[] rawOptions = {"To Do", "In Progress", "Completed"};
                new AlertDialog.Builder(v.getContext())
                    .setTitle("Ubah Status")
                    .setItems(options, (dialog, which) -> {
                        String newStatus = rawOptions[which];
                        FirebaseDatabase.getInstance("https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("tasks").child(task.getId()).child("status").setValue(newStatus);
                        FirebaseDatabase.getInstance("https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("tasks").child(task.getId()).child("progress").setValue(newStatus.equals("Completed") ? 100 : 0);
                    }).show();
            });

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                    .setTitle("Hapus Tugas")
                    .setMessage("Apakah Anda yakin ingin menghapus tugas ini?")
                    .setPositiveButton("Ya", (d, w) -> {
                        FirebaseDatabase.getInstance("https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference("tasks").child(task.getId()).removeValue();
                    }).setNegativeButton("Tidak", null).show();
            });

            btnDetail.setOnClickListener(v -> {
                TaskDetailFragment fragment = TaskDetailFragment.newInstance(
                    task.getTitle(), task.getProgress(), task.getDeadline(), task.getId());
                ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, fragment)
                    .addToBackStack(null)
                    .commit();
            });
        }
    }
}
