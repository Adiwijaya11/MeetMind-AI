package com.example.meetmindai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MeetingDetailFragment extends Fragment {

    private Meeting meeting;

    public static MeetingDetailFragment newInstance(Meeting meeting) {
        MeetingDetailFragment fragment = new MeetingDetailFragment();
        fragment.meeting = meeting;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting_detail, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDateTime = view.findViewById(R.id.tvDetailDateTime);
        TextView tvLocation = view.findViewById(R.id.tvDetailLocation);
        TextView tvNotes = view.findViewById(R.id.tvDetailNotes);

        if (meeting != null) {
            tvTitle.setText(meeting.getTitle());
            tvDateTime.setText(meeting.getDate() + " | " + meeting.getStartTime() + " - " + meeting.getEndTime());
            tvLocation.setText(meeting.getLocation());
            tvNotes.setText(meeting.getNotes());
        }

        return view;
    }
}
