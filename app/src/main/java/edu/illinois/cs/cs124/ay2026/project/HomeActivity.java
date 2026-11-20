package edu.illinois.cs.cs124.ay2026.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private PostAdapter adapter;
    private MaterialButton spinnerTopicFilter;
    private TextView emptyTitle;
    private TextView emptySubtitle;
    private TextView textFilterBanner;
    // allPosts holds every loaded post; posts holds the currently filtered view
    private final List<Post> allPosts = new ArrayList<>();
    private final List<Post> posts = new ArrayList<>();
    private String selectedTopic = null; // null means "All"
    private String searchQuery = null; // null means no search active
    private ListenerRegistration postsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        swipeRefresh = findViewById(R.id.swipeRefresh);
        RecyclerView recyclerPosts = findViewById(R.id.recyclerPosts);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        swipeRefresh.setOnRefreshListener(this::attachListener);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new PostAdapter(posts, currentUserId,
            this::openPostDetail,
            this::confirmDeletePost);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(adapter);

        spinnerTopicFilter = findViewById(R.id.spinnerTopicFilter);
        emptyTitle = layoutEmpty.findViewById(R.id.textEmptyTitle);
        emptySubtitle = layoutEmpty.findViewById(R.id.textEmptySubtitle);
        textFilterBanner = findViewById(R.id.textFilterBanner);
        textFilterBanner.setOnClickListener(v -> clearFilters());

        FloatingActionButton fabCreatePost = findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(v ->
            startActivity(new Intent(this, CreatePostActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (postsListener != null) {
            postsListener.remove();
            postsListener = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView =
            (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        if (searchView == null) return true;

        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.trim().isEmpty() ? null : newText.trim();
                applyFilter();
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchQuery = null;
                applyFilter();
                return true;
            }
        });

        // Restore search state after rotation
        if (searchQuery != null) {
            searchItem.expandActionView();
            searchView.setQuery(searchQuery, false);
            searchView.clearFocus();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_my_posts) {
            startActivity(new Intent(this, MyPostsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_edit_profile) {
            startActivity(new Intent(this, ProfileSetupActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                allPosts.remove(post);
                int index = posts.indexOf(post);
                if (index >= 0) {
                    posts.remove(index);
                    adapter.notifyItemRemoved(index);
                }
                if (posts.isEmpty()) {
                    swipeRefresh.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
                updateTopicSpinner();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, R.string.error_delete_failed, Toast.LENGTH_LONG).show());
    }

    // Returns a deduplicated list of uppercase topic labels from the given posts.
    // Case variants ("cs 124" / "CS 124") collapse into one entry.
    static List<String> buildTopicList(List<Post> all) {
        java.util.LinkedHashMap<String, String> topicMap = new java.util.LinkedHashMap<>();
        for (Post p : all) {
            if (p.getTopic() != null && !p.getTopic().isEmpty()) {
                topicMap.putIfAbsent(p.getTopic().toLowerCase(), p.getTopic().toUpperCase());
            }
        }
        return new ArrayList<>(topicMap.values());
    }

    // Returns posts matching both the topic filter and the search query.
    // Null topic means "all topics"; null/empty query means no text filter.
    // Search checks topic and notes fields (case-insensitive).
    static List<Post> filterPosts(List<Post> all, String topic, String query) {
        List<Post> result = new ArrayList<>();
        String lowerQuery = (query != null && !query.trim().isEmpty())
            ? query.trim().toLowerCase() : null;
        for (Post p : all) {
            if (topic != null && !topic.equalsIgnoreCase(p.getTopic())) continue;
            if (lowerQuery != null) {
                boolean topicMatch = p.getTopic() != null
                    && p.getTopic().toLowerCase().contains(lowerQuery);
                boolean notesMatch = p.getNotes() != null
                    && p.getNotes().toLowerCase().contains(lowerQuery);
                if (!topicMatch && !notesMatch) continue;
            }
            result.add(p);
        }
        return result;
    }

    // Rebuilds the spinner options from whatever is currently in allPosts.
    private void updateTopicSpinner() {
        List<String> topics = buildTopicList(allPosts);

        // Update button label to reflect current selection
        String allLabel = getString(R.string.filter_all_topics);
        spinnerTopicFilter.setText(selectedTopic == null
            ? allLabel
            : selectedTopic.toUpperCase());

        spinnerTopicFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, spinnerTopicFilter);
            popup.getMenu().add(allLabel);
            for (String topic : topics) {
                popup.getMenu().add(topic);
            }
            popup.setOnMenuItemClickListener(item -> {
                String picked = item.getTitle().toString();
                selectedTopic = picked.equals(allLabel) ? null : picked.toLowerCase();
                spinnerTopicFilter.setText(picked);
                applyFilter();
                return true;
            });
            popup.show();
        });
    }

    private void clearFilters() {
        selectedTopic = null;
        searchQuery = null;
        spinnerTopicFilter.setText(R.string.filter_all_topics);
        // Collapse the SearchView if it's open
        if (getSupportActionBar() != null) {
            invalidateOptionsMenu();
        }
        applyFilter();
    }

    // Filters allPosts by selectedTopic and searchQuery, then refreshes the RecyclerView.
    private void applyFilter() {
        posts.clear();
        posts.addAll(filterPosts(allPosts, selectedTopic, searchQuery));
        adapter.notifyDataSetChanged();

        // Show banner when any filter is active
        boolean topicActive = selectedTopic != null;
        boolean searchActive = searchQuery != null;
        if (topicActive && searchActive) {
            textFilterBanner.setText(R.string.filter_banner_both);
            textFilterBanner.setVisibility(View.VISIBLE);
        } else if (topicActive) {
            textFilterBanner.setText(R.string.filter_banner_topic);
            textFilterBanner.setVisibility(View.VISIBLE);
        } else if (searchActive) {
            textFilterBanner.setText(R.string.filter_banner_search);
            textFilterBanner.setVisibility(View.VISIBLE);
        } else {
            textFilterBanner.setVisibility(View.GONE);
        }
        if (posts.isEmpty()) {
            swipeRefresh.setVisibility(View.GONE);
            if (searchQuery != null) {
                emptyTitle.setText(R.string.empty_posts_search_title);
                emptySubtitle.setText(R.string.empty_posts_search_subtitle);
            } else if (selectedTopic != null) {
                emptyTitle.setText(R.string.empty_posts_filtered_title);
                emptySubtitle.setText(R.string.empty_posts_filtered_subtitle);
            } else {
                emptyTitle.setText(R.string.empty_posts_title);
                emptySubtitle.setText(R.string.empty_posts_subtitle);
            }
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            swipeRefresh.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void attachListener() {
        if (postsListener != null) {
            postsListener.remove();
        }

        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
            swipeRefresh.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        }

        Date oneWeekAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));

        postsListener = db.collection("posts")
            .whereGreaterThan("createdAt", new Timestamp(oneWeekAgo))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshot, error) -> {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (error != null || snapshot == null) {
                    if (error != null) {
                        Toast.makeText(this, R.string.error_load_posts_failed, Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                allPosts.clear();
                allPosts.addAll(snapshot.toObjects(Post.class));
                updateTopicSpinner();
                applyFilter();
            });
    }
}