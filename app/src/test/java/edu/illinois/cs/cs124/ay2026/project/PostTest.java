package edu.illinois.cs.cs124.ay2026.project;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PostTest {

    @Test
    public void noArgConstructor_doesNotCrash() {
        // Firestore deserialization requires a no-arg constructor.
        Post post = new Post();
        assertNull(post.getId());
        assertNull(post.getTopic());
        assertNull(post.getPosterName());
        assertNull(post.getPosterId());
        assertNull(post.getNotes());
        assertNull(post.getCreatedAt());
    }

    @Test
    public void settersAndGetters_roundTrip() {
        Post post = new Post();
        post.setId("abc123");
        post.setTopic("CS 124 Midterm");
        post.setPosterName("Liam");
        post.setPosterId("uid-42");
        post.setNotes("Meet at Grainger");
        post.setPartnerCount(3);

        assertEquals("abc123", post.getId());
        assertEquals("CS 124 Midterm", post.getTopic());
        assertEquals("Liam", post.getPosterName());
        assertEquals("uid-42", post.getPosterId());
        assertEquals("Meet at Grainger", post.getNotes());
        assertEquals(3, post.getPartnerCount());
    }

    @Test
    public void setPartnerCount_zero_isAccepted() {
        Post post = new Post();
        post.setPartnerCount(0);
        assertEquals(0, post.getPartnerCount());
    }
}