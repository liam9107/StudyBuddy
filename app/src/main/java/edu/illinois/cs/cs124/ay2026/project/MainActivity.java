package edu.illinois.cs.cs124.ay2026.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user is already signed in, skip the welcome screen and go straight to Home
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            navigateToHome();
            return;
        }

        setContentView(R.layout.activity_main);

        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        Button buttonLogIn = findViewById(R.id.buttonLogIn);

        buttonSignUp.setOnClickListener(v ->
            startActivity(new Intent(this, SignupActivity.class)));

        buttonLogIn.setOnClickListener(v ->
            startActivity(new Intent(this, LoginActivity.class)));
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}