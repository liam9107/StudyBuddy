package edu.illinois.cs.cs124.ay2026.project;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Post {

    @DocumentId
    private String id;
    private String topic;
    private int partnerCount;
    private String notes;
    private String posterId;
    private String posterName;
    private Timestamp createdAt;

    // Required no-arg constructor for Firestore deserialization
    public Post() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getPartnerCount() { return partnerCount; }
    public void setPartnerCount(int partnerCount) { this.partnerCount = partnerCount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPosterId() { return posterId; }
    public void setPosterId(String posterId) { this.posterId = posterId; }

    public String getPosterName() { return posterName; }
    public void setPosterName(String posterName) { this.posterName = posterName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}