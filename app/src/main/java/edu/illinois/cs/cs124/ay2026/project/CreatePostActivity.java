package edu.illinois.cs.cs124.ay2026.project;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private TextInputEditText editTopic;
    private TextInputEditText editPartnerCount;
    private TextInputEditText editNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTopic = findViewById(R.id.editTopic);
        editPartnerCount = findViewById(R.id.editPartnerCount);
        editNotes = findViewById(R.id.editNotes);

        Button buttonPost = findViewById(R.id.buttonPost);
        buttonPost.setOnClickListener(v -> submitPost());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void submitPost() {
        String topic = editTopic.getText() != null ? editTopic.getText().toString().trim() : "";
        String partnerCountStr = editPartnerCount.getText() != null ? editPartnerCount.getText().toString().trim() : "";
        String notes = editNotes.getText() != null ? editNotes.getText().toString().trim() : "";

        if (TextUtils.isEmpty(topic)) {
            editTopic.setError(getString(R.string.error_topic_required));
            return;
        }

        int partnerCount;
        try {
            partnerCount = Integer.parseInt(partnerCountStr);
            if (partnerCount < 1) {
                editPartnerCount.setError(getString(R.string.error_partner_count_min));
                return;
            }
        } catch (NumberFormatException e) {
            editPartnerCount.setError(getString(R.string.error_partner_count_invalid));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.error_not_signed_in, Toast.LENGTH_LONG).show();
            return;
        }

        Button buttonPost = findViewById(R.id.buttonPost);
        buttonPost.setEnabled(false);
        buttonPost.setText(R.string.button_posting);

        // Look up the user's name from Firestore so the post shows the correct display name.
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(doc -> {
                String posterName = doc.getString("name");
                if (TextUtils.isEmpty(posterName)) {
                    posterName = user.getEmail();
                }
                savePost(user.getUid(), posterName, topic, partnerCount, notes, buttonPost);
            })
            .addOnFailureListener(e -> {
                buttonPost.setEnabled(true);
                buttonPost.setText(R.string.button_post);
                Toast.makeText(this, R.string.error_load_profile_failed, Toast.LENGTH_LONG).show();
            });
    }

    private void savePost(String uid, String posterName, String topic, int partnerCount,
            String notes, Button buttonPost) {
        Map<String, Object> post = new HashMap<>();
        post.put("topic", topic);
        post.put("partnerCount", partnerCount);
        post.put("notes", notes);
        post.put("posterId", uid);
        post.put("posterName", posterName);
        post.put("createdAt", Timestamp.now());

        FirebaseFirestore.getInstance()
            .collection("posts")
            .add(post)
            .addOnSuccessListener(ref -> {
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                buttonPost.setEnabled(true);
                buttonPost.setText(R.string.button_post);
                Toast.makeText(this, R.string.error_post_failed, Toast.LENGTH_LONG).show();
            });
    }
}