package com.example.meetmindai;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DashboardFragment extends Fragment {

    private TextView tvCountRapat, tvCountTugas, tvMeetingTitle, tvMeetingTime, tvPengingatText;
    private LinearLayout llPengingatContainer;
    private MaterialCardView btnRapatBaru;
    private DatabaseReference mDatabaseMeetings, mDatabaseTasks;
    private static final String DB_URL = "https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String TAG = "DashboardFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvCountRapat = view.findViewById(R.id.tvCountRapat);
        tvCountTugas = view.findViewById(R.id.tvCountTugas);
        tvMeetingTitle = view.findViewById(R.id.tvMeetingTitle);
        tvMeetingTime = view.findViewById(R.id.tvMeetingTime);
        llPengingatContainer = view.findViewById(R.id.llPengingatContainer);
        tvPengingatText = view.findViewById(R.id.tvPengingatText);
        btnRapatBaru = view.findViewById(R.id.btnRapatBaru);

        mDatabaseMeetings = FirebaseDatabase.getInstance(DB_URL).getReference("meetings");
        mDatabaseTasks = FirebaseDatabase.getInstance(DB_URL).getReference("tasks");

        loadDashboardData();

        btnRapatBaru.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new AddMeetingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadDashboardData() {
        // Load Meetings
        mDatabaseMeetings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalMeetings = snapshot.getChildrenCount();
                tvCountRapat.setText(String.valueOf(totalMeetings));
                
                Meeting nextMeeting = null;
                Date now = new Date();
                long minDiff = Long.MAX_VALUE;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                for (DataSnapshot meetingSnapshot : snapshot.getChildren()) {
                    Meeting meeting = meetingSnapshot.getValue(Meeting.class);
                    if (meeting != null) {
                        try {
                            String timeToParse = (meeting.getStartTime() != null) ? meeting.getStartTime() : "00:00";
                            String meetingDateTime = meeting.getDate() + " " + timeToParse;
                            Date meetingDate = sdf.parse(meetingDateTime);
                            
                            if (meetingDate != null && meetingDate.after(now)) {
                                long diff = meetingDate.getTime() - now.getTime();
                                if (diff < minDiff) {
                                    minDiff = diff;
                                    nextMeeting = meeting;
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Error parsing meeting date", e); }
                    }
                }

                if (nextMeeting != null) {
                    llPengingatContainer.setVisibility(View.VISIBLE);
                    tvMeetingTitle.setText(nextMeeting.getTitle());
                    tvMeetingTime.setText(nextMeeting.getDate() + " | " + (nextMeeting.getStartTime() != null ? nextMeeting.getStartTime() : ""));
                    updateReminderText(nextMeeting);
                } else {
                    tvMeetingTitle.setText("Tidak ada rapat");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Meetings error", error.toException()); }
        });

        // Load Tasks and Check Deadlines
        mDatabaseTasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalTasks = snapshot.getChildrenCount();
                tvCountTugas.setText(String.valueOf(totalTasks));
                
                // Cari task terdekat
                TaskModel nearestTask = null;
                Date now = new Date();
                long minDiff = Long.MAX_VALUE;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    TaskModel task = taskSnapshot.getValue(TaskModel.class);
                    if (task != null && task.getDeadline() != null && !"Completed".equalsIgnoreCase(task.getStatus())) {
                        try {
                            Date deadlineDate = sdf.parse(task.getDeadline());
                            if (deadlineDate != null && deadlineDate.after(now)) {
                                long diff = deadlineDate.getTime() - now.getTime();
                                if (diff < minDiff) {
                                    minDiff = diff;
                                    nearestTask = task;
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Error parsing task deadline", e); }
                    }
                }

                // Prioritaskan pengingat meeting, tapi jika tidak ada, tampilkan pengingat tugas
                if (nearestTask != null && llPengingatContainer.getVisibility() == View.GONE) {
                    llPengingatContainer.setVisibility(View.VISIBLE);
                    tvMeetingTitle.setText("Tugas Mendatang: " + nearestTask.getTitle());
                    tvMeetingTime.setText("Deadline: " + nearestTask.getDeadline());
                    tvPengingatText.setText("Jangan lupa selesaikan tugas \"" + nearestTask.getTitle() + "\" sebelum " + nearestTask.getDeadline());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Tasks error", error.toException()); }
        });
    }

    private void updateReminderText(Meeting meeting) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date meetingDate = sdf.parse(meeting.getDate() + " " + (meeting.getStartTime() != null ? meeting.getStartTime() : "00:00"));
            Date now = new Date();
            if (meetingDate != null && meetingDate.after(now)) {
                long diffInMillis = meetingDate.getTime() - now.getTime();
                long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;
                tvPengingatText.setText("\"" + meeting.getTitle() + "\" dimulai dalam " + (hours > 0 ? hours + " jam " : "") + minutes + " menit.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
