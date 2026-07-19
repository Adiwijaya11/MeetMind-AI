package com.example.meetmindai;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTaskFragment extends Fragment {

    private EditText etTitle, etDate, etPic, etMeetingSource;
    private AutoCompleteTextView actvPriority, actvStatus;
    private Button btnSave;
    private DatabaseReference mDatabase;
    private static final String DB_URL = "https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("tasks");

        etTitle = view.findViewById(R.id.etTaskTitle);
        etDate = view.findViewById(R.id.etTaskDeadline);
        etPic = view.findViewById(R.id.etTaskPic);
        etMeetingSource = view.findViewById(R.id.etMeetingSource);
        actvPriority = view.findViewById(R.id.actvPriority);
        actvStatus = view.findViewById(R.id.actvStatus);
        btnSave = view.findViewById(R.id.btnSaveTask);

        String[] priorities = {"High", "Medium", "Low"};
        actvPriority.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, priorities));

        String[] statuses = {"To Do", "In Progress", "Completed"};
        actvStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses));

        // Listener untuk menampilkan dropdown saat diklik
        View.OnClickListener showDropdown = v -> {
            if (v instanceof AutoCompleteTextView) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        };

        actvPriority.setOnClickListener(showDropdown);
        actvStatus.setOnClickListener(showDropdown);

        etDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            etDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Judul harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("deadline", etDate.getText().toString());
        task.put("pic", etPic.getText().toString());
        task.put("meetingSource", etMeetingSource.getText().toString());
        task.put("priority", actvPriority.getText().toString());
        task.put("status", actvStatus.getText().toString());
        task.put("progress", actvStatus.getText().toString().equals("Completed") ? 100 : 0);

        mDatabase.push().setValue(task).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });
    }
}
