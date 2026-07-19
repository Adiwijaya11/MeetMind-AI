package com.example.meetmindai;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private EditText etNamaLengkap, etEmail, etPhone, etBio;
    private TextView tvUsernameHeader, tvEmailHeader, tvChangePhoto, tvLogout;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) uploadImageToFirebase(imageUri);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openGallery();
                else Toast.makeText(getActivity(), "Izin akses galeri diperlukan", Toast.LENGTH_SHORT).show();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://meetmind-ai-9a728-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        
        // Use the explicit bucket URL to avoid 404 errors if default bucket is not set correctly
        mStorage = FirebaseStorage.getInstance("gs://meetmind-ai-9a728.appspot.com").getReference().child("profile_images");

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        etNamaLengkap = view.findViewById(R.id.etNamaLengkap);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etBio = view.findViewById(R.id.etBio);
        tvUsernameHeader = view.findViewById(R.id.tvUsernameHeader);
        tvEmailHeader = view.findViewById(R.id.tvEmailHeader);
        
        tvChangePhoto = view.findViewById(R.id.tvChangePhoto);
        tvLogout = view.findViewById(R.id.tvLogout);
        btnSave = view.findViewById(R.id.btnSave);

        loadUserData();

        View.OnClickListener imageClickListener = v -> checkPermissionAndOpenGallery();
        ivProfilePicture.setOnClickListener(imageClickListener);
        tvChangePhoto.setOnClickListener(imageClickListener);

        btnSave.setOnClickListener(v -> showSuccessDialog());

        tvLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        return view;
    }

    private void checkPermissionAndOpenGallery() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? 
                          Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri uri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            StorageReference fileRef = mStorage.child(user.getUid() + ".jpg");
            
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            fileRef.putFile(uri, metadata).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    mDatabase.child(user.getUid()).child("profileImageUrl").setValue(uri1.toString());
                    Glide.with(this).load(uri1).into(ivProfilePicture);
                    Toast.makeText(getActivity(), "Foto berhasil diperbarui", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Gagal mendapatkan download URL", e);
                    Toast.makeText(getActivity(), "Gagal memproses foto", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e("ProfileFragment", "Gagal upload: ", e);
                
                String errorMessage = "Gagal upload: " + e.getLocalizedMessage();
                if (e instanceof StorageException) {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_NOT_AUTHORIZED) {
                        errorMessage = "Tidak memiliki izin untuk mengunggah.";
                    } else if (errorCode == StorageException.ERROR_RETRY_LIMIT_EXCEEDED) {
                        errorMessage = "Koneksi tidak stabil, coba lagi.";
                    } else if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        errorMessage = "Lokasi penyimpanan tidak ditemukan.";
                    }
                }
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            });
        }
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmailHeader.setText(user.getEmail());
            etEmail.setText(user.getEmail());
            mDatabase.child(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    String name = snapshot.child("name").getValue(String.class);
                    etNamaLengkap.setText(name);
                    tvUsernameHeader.setText(name);
                    etPhone.setText(snapshot.child("phone").getValue(String.class));
                    etBio.setText(snapshot.child("bio").getValue(String.class));
                    
                    String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).into(ivProfilePicture);
                    }
                }
            });
        }
    }

    private void showSuccessDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String name = etNamaLengkap.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (name.isEmpty()) {
            etNamaLengkap.setError("Nama tidak boleh kosong");
            return;
        }

        mDatabase.child(user.getUid()).child("name").setValue(name);
        mDatabase.child(user.getUid()).child("phone").setValue(phone);
        mDatabase.child(user.getUid()).child("bio").setValue(bio);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_success, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnOk).setOnClickListener(v -> {
            loadUserData();
            dialog.dismiss();
        });
        dialog.show();
    }
}
