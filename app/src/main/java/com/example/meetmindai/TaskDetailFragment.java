package com.example.meetmindai;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;

public class TaskDetailFragment extends Fragment {

    private String title, deadline, taskId;
    private int progress;

    public static TaskDetailFragment newInstance(String title, int progress, String deadline, String taskId) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("progress", progress);
        args.putString("deadline", deadline);
        args.putString("taskId", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail, container, false);

        if (getArguments() != null) {
            title = getArguments().getString("title");
            progress = getArguments().getInt("progress");
            deadline = getArguments().getString("deadline");
            taskId = getArguments().getString("taskId");
        }

        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvProgress = view.findViewById(R.id.tvDetailProgress);
        TextView tvDeadline = view.findViewById(R.id.tvDetailDeadline);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        Button btnEditStatus = view.findViewById(R.id.btnEditStatus); // Pastikan ada ID ini di XML

        tvTitle.setText(title);
        tvProgress.setText(progress + "%");
        tvDeadline.setText(deadline);

        // Status styling (Simplified)
        refreshStatusStyle(tvStatus, progress >= 100 ? "Completed" : "In Progress");

        btnEditStatus.setOnClickListener(v -> {
            String[] options = {"To Do", "In Progress", "Completed"};
            new AlertDialog.Builder(getContext())
                .setTitle("Ubah Status")
                .setItems(options, (dialog, which) -> {
                    String newStatus = options[which];
                    FirebaseDatabase.getInstance("https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("tasks").child(taskId).child("status").setValue(newStatus);
                    FirebaseDatabase.getInstance("https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("tasks").child(taskId).child("progress").setValue(newStatus.equals("Completed") ? 100 : 0);
                    tvStatus.setText(newStatus);
                    refreshStatusStyle(tvStatus, newStatus);
                    Toast.makeText(getContext(), "Status diperbarui", Toast.LENGTH_SHORT).show();
                })
                .show();
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void refreshStatusStyle(TextView tvStatus, String status) {
        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setShape(GradientDrawable.RECTANGLE);
        statusBg.setCornerRadius(20);
        int color = status.equals("Completed") ? Color.parseColor("#4CAF50") : Color.parseColor("#FF9800");
        statusBg.setColor(color);
        tvStatus.setBackground(statusBg);
        tvStatus.setText(status);
    }
}
