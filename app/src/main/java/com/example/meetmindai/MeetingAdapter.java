package com.example.meetmindai;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    public interface OnItemClickListener {
        void onDeleteClick(Meeting meeting);
        void onItemClick(Meeting meeting);
    }

    private final List<Meeting> meetingList;
    private final OnItemClickListener listener;

    public MeetingAdapter(List<Meeting> meetingList, OnItemClickListener listener) {
        this.meetingList = meetingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);
        holder.bind(meeting, listener);
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDate, tvTime, tvLocation, tvParticipants, tvStatusBadge;
        private final Button btnDetail;
        private final ImageView btnDelete;

        MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMeetingTitle);
            tvDate = itemView.findViewById(R.id.tvMeetingDate);
            tvTime = itemView.findViewById(R.id.tvMeetingTime);
            tvLocation = itemView.findViewById(R.id.tvMeetingLocation);
            tvParticipants = itemView.findViewById(R.id.tvMeetingParticipants);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnDetail = itemView.findViewById(R.id.btnDetailMeeting);
            btnDelete = itemView.findViewById(R.id.btnDeleteMeeting);
        }

        void bind(Meeting meeting, OnItemClickListener listener) {
            tvTitle.setText(meeting.getTitle());
            tvDate.setText(meeting.getDate());
            tvTime.setText(meeting.getTime());
            tvLocation.setText(meeting.getLocation());
            tvParticipants.setText(meeting.getParticipants() + " peserta");
            
            String status = meeting.getStatus();
            if (status == null) status = "Unknown";
            tvStatusBadge.setText(status);

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(20);

            switch (status) {
                case "Berlangsung": badgeBg.setColor(0xFF4CAF50); break;
                case "Hari Ini": badgeBg.setColor(0xFF2196F3); break;
                case "Akan Datang": badgeBg.setColor(0xFFFF9800); break;
                case "Selesai": badgeBg.setColor(0xFF9E9E9E); break;
                case "Dibatalkan": badgeBg.setColor(0xFFF44336); break;
                default: badgeBg.setColor(0xFF9E9E9E); break;
            }
            tvStatusBadge.setBackground(badgeBg);

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(meeting);
            });

            btnDetail.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(meeting);
            });
        }
    }
}
