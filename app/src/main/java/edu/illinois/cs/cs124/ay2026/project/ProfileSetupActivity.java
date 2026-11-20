package edu.illinois.cs.cs124.ay2026.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private TextInputEditText editName;
    private TextInputEditText editPhone;
    private TextInputEditText editDiscord;
    private Button buttonSave;
    // True when opened from the Home menu to edit an existing profile.
    // False when opened after signup for first-time setup.
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editDiscord = findViewById(R.id.editDiscord);
        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> saveProfile());

        String nameFromSignup = getIntent().getStringExtra("name");
        isEditMode = TextUtils.isEmpty(nameFromSignup);

        if (isEditMode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_edit_profile);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            loadExistingProfile();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_profile_setup);
            }
            editName.setText(nameFromSignup);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadExistingProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) return;
                String name = doc.getString("name");
                String phone = doc.getString("phone");
                String discord = doc.getString("discord");
                if (!TextUtils.isEmpty(name)) editName.setText(name);
                if (!TextUtils.isEmpty(phone)) editPhone.setText(phone);
                if (!TextUtils.isEmpty(discord)) editDiscord.setText(discord);
            });
    }

    private void saveProfile() {
        String name = editName.getText() != null ? editName.getText().toString().trim() : "";
        String phone = editPhone.getText() != null ? editPhone.getText().toString().trim() : "";
        String discord = editDiscord.getText() != null ? editDiscord.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            editName.setError(getString(R.string.error_name_required));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.error_not_signed_in, Toast.LENGTH_LONG).show();
            return;
        }

        buttonSave.setEnabled(false);
        buttonSave.setText(R.string.button_saving);

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("email", user.getEmail());
        profile.put("phone", phone);
        profile.put("discord", discord);

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .set(profile)
            .addOnSuccessListener(unused -> {
                if (isEditMode) {
                    finish();
                } else {
                    navigateToHome();
                }
            })
            .addOnFailureListener(e -> {
                buttonSave.setEnabled(true);
                buttonSave.setText(R.string.button_save_profile);
                Toast.makeText(this, R.string.error_save_profile_failed, Toast.LENGTH_LONG).show();
            });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}