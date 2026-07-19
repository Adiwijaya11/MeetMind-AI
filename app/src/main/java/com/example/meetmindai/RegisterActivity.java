package com.example.meetmindai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etUsername, etPassword, etBirthDate, etAddress, etPhone;
    private Button btnRegister;
    private TextView tvLogin;
    private CheckBox cbTerms;
    private static final String DB_URL = "https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etBirthDate = findViewById(R.id.etBirthDate);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        cbTerms = findViewById(R.id.cbTerms);

        setupTermsText();

        btnRegister.setOnClickListener(v -> registerToFirebase());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }

    private void registerToFirebase() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Username wajib diisi");
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Masukkan email valid");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Harap setujui ketentuan layanan", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    User user = new User(username, email, birthDate, address, phone);
                    
                    FirebaseDatabase.getInstance(DB_URL).getReference("users")
                        .child(userId)
                        .setValue(user)
                        .addOnCompleteListener(dbTask -> {
                            btnRegister.setEnabled(true);
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, DashboardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Gagal simpan profil", Toast.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Registrasi Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void setupTermsText() {
        String fullText = "Saya menyetujui Ketentuan Layanan dan Kebijakan Privasi";
        SpannableString spannableString = new SpannableString(fullText);
        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) { Toast.makeText(RegisterActivity.this, "Ketentuan Layanan diklik", Toast.LENGTH_SHORT).show(); }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) { super.updateDrawState(ds); ds.setColor(Color.parseColor("#FF9800")); ds.setUnderlineText(true); }
        };
        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) { Toast.makeText(RegisterActivity.this, "Kebijakan Privasi diklik", Toast.LENGTH_SHORT).show(); }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) { super.updateDrawState(ds); ds.setColor(Color.parseColor("#FF9800")); ds.setUnderlineText(true); }
        };
        spannableString.setSpan(termsSpan, 16, 33, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacySpan, 37, 55, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        cbTerms.setText(spannableString);
        cbTerms.setMovementMethod(LinkMovementMethod.getInstance());
        cbTerms.setHighlightColor(Color.TRANSPARENT);
    }
}
