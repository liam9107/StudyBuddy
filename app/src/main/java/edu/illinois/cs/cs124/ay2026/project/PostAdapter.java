package edu.illinois.cs.cs124.ay2026.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public interface OnPostDeleteListener {
        void onPostDelete(Post post);
    }

    private final List<Post> posts;
    private final String currentUserId;
    private final OnPostClickListener clickListener;
    private final OnPostDeleteListener deleteListener;

    public PostAdapter(List<Post> posts, String currentUserId,
            OnPostClickListener clickListener, OnPostDeleteListener deleteListener) {
        this.posts = posts;
        this.currentUserId = currentUserId;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position), currentUserId, clickListener, deleteListener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        private final TextView textTopic;
        private final TextView textPartnerCount;
        private final TextView textPosterName;
        private final TextView textTimeRemaining;
        private final ImageButton buttonDelete;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textTopic = itemView.findViewById(R.id.textTopic);
            textPartnerCount = itemView.findViewById(R.id.textPartnerCount);
            textPosterName = itemView.findViewById(R.id.textPosterName);
            textTimeRemaining = itemView.findViewById(R.id.textTimeRemaining);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(Post post, String currentUserId,
                OnPostClickListener clickListener, OnPostDeleteListener deleteListener) {
            textTopic.setText(post.getTopic());
            textPartnerCount.setText(formatPartnerCount(post.getPartnerCount()));
            textPosterName.setText(
                itemView.getContext().getString(R.string.posted_by_format, post.getPosterName()));
            Timestamp createdAt = post.getCreatedAt();
            textTimeRemaining.setText(createdAt == null ? ""
                : formatTimeRemaining(createdAt.toDate().getTime(), new Date().getTime()));
            itemView.setOnClickListener(v -> clickListener.onPostClick(post));

            if (post.getPosterId() != null && post.getPosterId().equals(currentUserId)) {
                buttonDelete.setVisibility(View.VISIBLE);
                buttonDelete.setOnClickListener(v -> deleteListener.onPostDelete(post));
            } else {
                buttonDelete.setVisibility(View.GONE);
            }
        }

        static String formatPartnerCount(int count) {
            return count == 1 ? "Looking for 1 partner" : "Looking for " + count + " partners";
        }

        // Package-private and takes explicit 'nowMs' so tests can control the clock.
        static String formatTimeRemaining(long createdAtMs, long nowMs) {
            long expiresAtMs = createdAtMs + TimeUnit.DAYS.toMillis(7);
            long millisRemaining = expiresAtMs - nowMs;
            long hoursRemaining = TimeUnit.MILLISECONDS.toHours(millisRemaining);
            if (hoursRemaining >= 48) {
                return TimeUnit.MILLISECONDS.toDays(millisRemaining) + "d left";
            } else if (hoursRemaining >= 1) {
                return hoursRemaining + "h left";
            } else {
                return "< 1h left";
            }
        }
    }
}