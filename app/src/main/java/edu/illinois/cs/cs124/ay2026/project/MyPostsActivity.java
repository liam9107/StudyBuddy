package edu.illinois.cs.cs124.ay2026.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyPostsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerPosts;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private PostAdapter adapter;
    private final List<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        recyclerPosts = findViewById(R.id.recyclerPosts);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new PostAdapter(posts, currentUserId,
            this::openPostDetail,
            this::confirmDeletePost);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(adapter);

        loadMyPosts(currentUserId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void openPostDetail(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_TOPIC, post.getTopic());
        intent.putExtra(PostDetailActivity.EXTRA_PARTNER_COUNT, post.getPartnerCount());
        intent.putExtra(PostDetailActivity.EXTRA_NOTES, post.getNotes());
        intent.putExtra(PostDetailActivity.EXTRA_POSTER_ID, post.getPosterId());
        startActivity(intent);
    }

    private void confirmDeletePost(Post post) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_post_confirm_title)
            .setMessage(R.string.delete_post_confirm_message)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> deletePost(post))
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void deletePost(Post post) {
        db.collection("posts").document(post.getId())
            .delete()
            .addOnSuccessListener(unused -> {
                int index = posts.indexOf(post);
                if (index >= 0) {
                    posts.remove(index);
                    adapter.notifyItemRemoved(index);
                }
                if (posts.isEmpty()) {
                    recyclerPosts.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, R.string.error_delete_failed, Toast.LENGTH_LONG).show());
    }

    private void loadMyPosts(String currentUserId) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerPosts.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        Date oneWeekAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));

        db.collection("posts")
            .whereEqualTo("posterId", currentUserId)
            .whereGreaterThan("createdAt", new Timestamp(oneWeekAgo))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                posts.clear();
                posts.addAll(snapshot.toObjects(Post.class));
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if (posts.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerPosts.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.error_load_posts_failed, Toast.LENGTH_LONG).show();
            });
    }
}