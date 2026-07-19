package com.example.meetmindai;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OtpActivity extends AppCompatActivity {

    private EditText[] etOtps = new EditText[4];
    private Button btnVerify;
    private TextView tvResend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOtps[0] = findViewById(R.id.etOtp1);
        etOtps[1] = findViewById(R.id.etOtp2);
        etOtps[2] = findViewById(R.id.etOtp3);
        etOtps[3] = findViewById(R.id.etOtp4);
        btnVerify = findViewById(R.id.btnVerify);
        tvResend = findViewById(R.id.tvResend);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            etOtps[i].addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 3) {
                        etOtps[index + 1].requestFocus();
                    }
                }
                public void afterTextChanged(Editable s) {}
            });
        }

        btnVerify.setOnClickListener(v -> {
            StringBuilder otp = new StringBuilder();
            for (EditText et : etOtps) {
                otp.append(et.getText().toString());
            }
            if (otp.length() == 4) {
                Toast.makeText(this, "Kode berhasil diverifikasi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Masukkan lengkap 4 digit", Toast.LENGTH_SHORT).show();
            }
        });

        tvResend.setOnClickListener(v -> {
            Toast.makeText(this, "Kode dikirim ulang", Toast.LENGTH_SHORT).show();
        });
    }
}
