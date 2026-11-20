package edu.illinois.cs.cs124.ay2026.project;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostDetailActivity extends AppCompatActivity {

    static final String EXTRA_TOPIC = "topic";
    static final String EXTRA_PARTNER_COUNT = "partnerCount";
    static final String EXTRA_NOTES = "notes";
    static final String EXTRA_POSTER_ID = "posterId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_post_detail);
        }

        String topic = getIntent().getStringExtra(EXTRA_TOPIC);
        int partnerCount = getIntent().getIntExtra(EXTRA_PARTNER_COUNT, 0);
        String notes = getIntent().getStringExtra(EXTRA_NOTES);
        String posterId = getIntent().getStringExtra(EXTRA_POSTER_ID);

        TextView textTopic = findViewById(R.id.textTopic);
        TextView textPartnerCount = findViewById(R.id.textPartnerCount);
        TextView textNotes = findViewById(R.id.textNotes);

        textTopic.setText(topic);
        textPartnerCount.setText(PostAdapter.PostViewHolder.formatPartnerCount(partnerCount));

        if (!TextUtils.isEmpty(notes)) {
            textNotes.setText(notes);
            textNotes.setVisibility(View.VISIBLE);
        }

        loadContactInfo(posterId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadContactInfo(String posterId) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ScrollView scrollContent = findViewById(R.id.scrollContent);

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(posterId)
            .get()
            .addOnSuccessListener(doc -> {
                progressBar.setVisibility(View.GONE);
                scrollContent.setVisibility(View.VISIBLE);

                String name = doc.getString("name");
                String email = doc.getString("email");
                String phone = doc.getString("phone");
                String discord = doc.getString("discord");

                TextView textName = findViewById(R.id.textName);
                TextView textEmail = findViewById(R.id.textEmail);
                TextView textPhone = findViewById(R.id.textPhone);
                TextView textDiscord = findViewById(R.id.textDiscord);
                TextView textNoContact = findViewById(R.id.textNoContact);

                if (!TextUtils.isEmpty(name)) {
                    textName.setText(name);
                }

                boolean hasContact = false;
                if (!TextUtils.isEmpty(email)) {
                    textEmail.setText(getString(R.string.email_format, email));
                    textEmail.setVisibility(View.VISIBLE);
                    hasContact = true;
                }
                if (!TextUtils.isEmpty(phone)) {
                    textPhone.setText(getString(R.string.phone_format, phone));
                    textPhone.setVisibility(View.VISIBLE);
                    hasContact = true;
                }
                if (!TextUtils.isEmpty(discord)) {
                    textDiscord.setText(getString(R.string.discord_format, discord));
                    textDiscord.setVisibility(View.VISIBLE);
                    hasContact = true;
                }
                if (!hasContact) {
                    textNoContact.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.error_load_contact_failed, Toast.LENGTH_LONG).show();
                finish();
            });
    }
}