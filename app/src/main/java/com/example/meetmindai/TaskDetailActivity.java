package com.example.meetmindai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText etTitle, etProgress, etDeadline;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        etTitle = findViewById(R.id.etDetailTitle);
        etProgress = findViewById(R.id.etDetailProgress);
        etDeadline = findViewById(R.id.etDetailDeadline);
        btnSave = findViewById(R.id.btnSaveDetail);

        btnSave.setOnClickListener(v -> {
            int progress = Integer.parseInt(etProgress.getText().toString());
            String deadline = etDeadline.getText().toString();
            
            // Logika Selesai Otomatis
            if (progress >= 100 || isDateReached(deadline)) {
                // Update ke status "Completed" di Firebase nanti
                Toast.makeText(this, "Status diupdate ke Completed", Toast.LENGTH_SHORT).show();
            }
            
            Toast.makeText(this, "Detail disimpan", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private boolean isDateReached(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date deadline = sdf.parse(dateStr);
            return new Date().after(deadline);
        } catch (Exception e) {
            return false;
        }
    }
}
