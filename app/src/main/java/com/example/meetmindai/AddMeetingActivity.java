package com.example.meetmindai;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMeetingActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDate, etStartTime, etEndTime, etLocation, etNotes;
    // private AutoCompleteTextView etCategory;
    private SwitchMaterial switchAi, switchReminder;
    private Button btnSave;
    private DatabaseReference mDatabase;
    private static final String DB_URL = "https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meeting);

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("meetings");

        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etLocation = findViewById(R.id.etLocation);
        // etCategory = findViewById(R.id.etCategory);
        etNotes = findViewById(R.id.etNotes);
        switchAi = findViewById(R.id.switchAi);
        switchReminder = findViewById(R.id.switchReminder);
        btnSave = findViewById(R.id.btnSave);

        // String[] categories = {"Daily Standup", "Weekly Meeting", "Client Meeting", "Project Meeting", "Brainstorming", "Interview", "Agenda Meeting"};
        // ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        // etCategory.setAdapter(adapter);

        etDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        btnSave.setOnClickListener(v -> saveMeeting());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> 
            etDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year)), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText field) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> 
            field.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute)), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void saveMeeting() {
        String title = etTitle.getText().toString();
        if (title.isEmpty()) { etTitle.setError("Required"); return; }

        Map<String, Object> meeting = new HashMap<>();
        meeting.put("title", title);
        meeting.put("date", etDate.getText().toString());
        meeting.put("startTime", etStartTime.getText().toString());
        meeting.put("endTime", etEndTime.getText().toString());
        meeting.put("location", etLocation.getText().toString());
        // meeting.put("category", etCategory.getText().toString());
        meeting.put("notes", etNotes.getText().toString());
        meeting.put("useAi", switchAi.isChecked());
        meeting.put("useReminder", switchReminder.isChecked());
        meeting.put("status", "upcoming");

        mDatabase.push().setValue(meeting).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Meeting saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
