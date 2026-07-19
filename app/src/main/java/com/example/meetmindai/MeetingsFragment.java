package com.example.meetmindai;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetingsFragment extends Fragment {

    private RecyclerView rvMeetings;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddMeeting;
    private ChipGroup chipGroupFilter;
    private EditText etSearch;
    private MeetingAdapter adapter;
    private List<Meeting> meetingList = new ArrayList<>();
    private List<Meeting> filteredList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private static final String DB_URL = "https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meetings, container, false);

        rvMeetings = view.findViewById(R.id.rvMeetings);
        emptyState = view.findViewById(R.id.emptyState);
        fabAddMeeting = view.findViewById(R.id.fabAddMeeting);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        etSearch = view.findViewById(R.id.etSearch);

        rvMeetings.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        adapter = new MeetingAdapter(filteredList, new MeetingAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(Meeting meeting) {
                showDeleteConfirmationDialog(meeting);
            }

            @Override
            public void onItemClick(Meeting meeting) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, MeetingDetailFragment.newInstance(meeting))
                        .addToBackStack(null)
                        .commit();
            }
        });
        rvMeetings.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("meetings");

        loadMeetings();

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabAddMeeting.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new AddMeetingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void showDeleteConfirmationDialog(Meeting meeting) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hapus Rapat")
                .setMessage("Apakah Anda yakin ingin menghapus rapat \"" + meeting.getTitle() + "\"?")
                .setNeutralButton("Batal", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Hapus", (dialog, which) -> {
                    if (meeting.getId() != null) {
                        mDatabase.child(meeting.getId()).removeValue();
                    }
                })
                .show();
    }

    private void loadMeetings() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                meetingList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Meeting meeting = data.getValue(Meeting.class);
                    if (meeting != null) {
                        meeting.setId(data.getKey());
                        meetingList.add(meeting);
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyFilters() {
        filteredList.clear();
        String query = etSearch.getText().toString().toLowerCase().trim();
        Chip chip = chipGroupFilter.findViewById(chipGroupFilter.getCheckedChipId());
        String filter = (chip != null) ? chip.getText().toString() : "Semua";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String todayStr = sdf.format(new Date());

        try {
            Date today = sdf.parse(todayStr);

            for (Meeting m : meetingList) {
                boolean matchesSearch = m.getTitle().toLowerCase().contains(query);
                boolean matchesChip = false;

                if (filter.equals("Semua")) matchesChip = true;
                else if (filter.equals("Hari Ini")) matchesChip = todayStr.equals(m.getDate());
                else if (filter.equals("Akan Datang")) {
                    Date meetingDate = sdf.parse(m.getDate());
                    matchesChip = meetingDate != null && meetingDate.after(today);
                } else if (filter.equals("Selesai")) {
                    Date meetingDate = sdf.parse(m.getDate());
                    matchesChip = meetingDate != null && (meetingDate.before(today) || todayStr.equals(m.getDate()));
                } else {
                    matchesChip = true; // For other filters like Berlangsung/Dibatalkan, apply as needed
                }

                if (matchesSearch && matchesChip) {
                    filteredList.add(m);
                }
            }
        } catch (ParseException e) { e.printStackTrace(); }
        
        adapter.notifyDataSetChanged();
        emptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        rvMeetings.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
