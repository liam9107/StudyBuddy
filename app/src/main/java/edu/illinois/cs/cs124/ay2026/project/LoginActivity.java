package edu.illinois.cs.cs124.ay2026.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private Button buttonLogIn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        buttonLogIn = findViewById(R.id.buttonLogIn);
        buttonLogIn.setOnClickListener(v -> logIn());
        progressBar = findViewById(R.id.progressBar);

        TextView textForgotPassword = findViewById(R.id.textForgotPassword);
        textForgotPassword.setOnClickListener(v -> sendPasswordReset());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void logIn() {
        String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
        String password = editPassword.getText() != null ? editPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_email_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editPassword.setError(getString(R.string.error_password_required));
            return;
        }

        buttonLogIn.setEnabled(false);
        buttonLogIn.setText(R.string.button_logging_in);
        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> checkProfileAndNavigate())
            .addOnFailureListener(e -> {
                buttonLogIn.setEnabled(true);
                buttonLogIn.setText(R.string.button_log_in);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_LONG).show();
            });
    }

    // After login, check if the user has completed profile setup.
    // New users who skipped it get routed there; returning users go straight to Home.
    private void checkProfileAndNavigate() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navigateToHome();
            return;
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    navigateToHome();
                } else {
                    navigateToProfileSetup();
                }
            })
            .addOnFailureListener(e -> navigateToHome());
    }

    private void sendPasswordReset() {
        String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_email_for_reset));
            return;
        }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener(unused ->
                Toast.makeText(this, R.string.message_reset_email_sent, Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e ->
                Toast.makeText(this, R.string.error_password_reset_failed, Toast.LENGTH_LONG).show());
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToProfileSetup() {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}