package edu.illinois.cs.cs124.ay2026.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText editName;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private Button buttonSignUp;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(v -> signUp());
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void signUp() {
        String name = editName.getText() != null ? editName.getText().toString().trim() : "";
        String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
        String password = editPassword.getText() != null ? editPassword.getText().toString() : "";

        if (TextUtils.isEmpty(name)) {
            editName.setError(getString(R.string.error_name_required));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_email_required));
            return;
        }
        if (password.length() < 6) {
            editPassword.setError(getString(R.string.error_password_too_short));
            return;
        }

        buttonSignUp.setEnabled(false);
        buttonSignUp.setText(R.string.button_signing_up);
        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> navigateToProfileSetup(name))
            .addOnFailureListener(e -> {
                buttonSignUp.setEnabled(true);
                buttonSignUp.setText(R.string.button_sign_up);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.error_create_account_failed, Toast.LENGTH_LONG).show();
            });
    }

    private void navigateToProfileSetup(String name) {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }
}