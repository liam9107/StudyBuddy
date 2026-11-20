package edu.illinois.cs.cs124.ay2026.project;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HomeActivityTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Post postWithTopic(String topic) {
        Post p = new Post();
        p.setTopic(topic);
        return p;
    }

    private static Post postWithTopicAndNotes(String topic, String notes) {
        Post p = new Post();
        p.setTopic(topic);
        p.setNotes(notes);
        return p;
    }

    // ── filterPosts — topic only ──────────────────────────────────────────────

    @Test
    public void filterPosts_nullTopic_returnsAll() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("MATH 231"));
        List<Post> result = HomeActivity.filterPosts(all, null, null);
        assertEquals(2, result.size());
    }

    @Test
    public void filterPosts_matchingTopic_returnsOnlyMatches() {
        Post cs = postWithTopic("CS 124");
        Post math = postWithTopic("MATH 231");
        List<Post> result = HomeActivity.filterPosts(Arrays.asList(cs, math), "cs 124", null);
        assertEquals(1, result.size());
        assertEquals("CS 124", result.get(0).getTopic());
    }

    @Test
    public void filterPosts_caseInsensitive_matchesRegardlessOfCase() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("cs 124"), postWithTopic("Cs 124"));
        List<Post> result = HomeActivity.filterPosts(all, "cs 124", null);
        assertEquals(3, result.size());
    }

    @Test
    public void filterPosts_noMatches_returnsEmptyList() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("MATH 231"));
        List<Post> result = HomeActivity.filterPosts(all, "phys 101", null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterPosts_emptyList_returnsEmptyList() {
        List<Post> result = HomeActivity.filterPosts(Collections.emptyList(), "cs 124", null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterPosts_doesNotMutateOriginalList() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("MATH 231"));
        HomeActivity.filterPosts(all, "cs 124", null);
        assertEquals(2, all.size());
    }

    // ── filterPosts — search query ────────────────────────────────────────────

    @Test
    public void filterPosts_queryMatchesTopic_returnsMatch() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("MATH 231"));
        List<Post> result = HomeActivity.filterPosts(all, null, "math");
        assertEquals(1, result.size());
        assertEquals("MATH 231", result.get(0).getTopic());
    }

    @Test
    public void filterPosts_queryMatchesNotes_returnsMatch() {
        Post p1 = postWithTopicAndNotes("CS 124", "Meet at Grainger");
        Post p2 = postWithTopicAndNotes("MATH 231", "Meet at Main Library");
        List<Post> result = HomeActivity.filterPosts(Arrays.asList(p1, p2), null, "grainger");
        assertEquals(1, result.size());
        assertEquals("CS 124", result.get(0).getTopic());
    }

    @Test
    public void filterPosts_nullNotes_doesNotCrash() {
        // Notes are optional — a post with null notes should never crash the search.
        Post p = postWithTopic("CS 124"); // notes not set, defaults to null
        List<Post> result = HomeActivity.filterPosts(Collections.singletonList(p), null, "grainger");
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterPosts_nullNotes_stillMatchesOnTopic() {
        Post p = postWithTopic("CS 124"); // notes are null
        List<Post> result = HomeActivity.filterPosts(Collections.singletonList(p), null, "cs");
        assertEquals(1, result.size());
    }

    @Test
    public void filterPosts_queryIsCaseInsensitive() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("MATH 231"));
        List<Post> result = HomeActivity.filterPosts(all, null, "CS");
        assertEquals(1, result.size());
    }

    @Test
    public void filterPosts_topicAndQueryBothApply() {
        Post p1 = postWithTopicAndNotes("CS 124", "Meet at Grainger");
        Post p2 = postWithTopicAndNotes("CS 124", "Meet at Union");
        Post p3 = postWithTopicAndNotes("MATH 231", "Meet at Grainger");
        List<Post> result = HomeActivity.filterPosts(Arrays.asList(p1, p2, p3), "cs 124", "grainger");
        assertEquals(1, result.size());
        assertEquals("CS 124", result.get(0).getTopic());
    }

    @Test
    public void filterPosts_emptyQuery_treatedAsNoFilter() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("MATH 231"));
        List<Post> result = HomeActivity.filterPosts(all, null, "  ");
        assertEquals(2, result.size());
    }

    @Test
    public void filterPosts_queryNoMatches_returnsEmptyList() {
        List<Post> all = Arrays.asList(postWithTopic("CS 124"), postWithTopic("MATH 231"));
        List<Post> result = HomeActivity.filterPosts(all, null, "zzz");
        assertTrue(result.isEmpty());
    }

    // ── buildTopicList ────────────────────────────────────────────────────────

    @Test
    public void buildTopicList_uniqueTopics_returnsAllUppercased() {
        List<Post> all = Arrays.asList(postWithTopic("cs 124"), postWithTopic("math 231"));
        List<String> topics = HomeActivity.buildTopicList(all);
        assertEquals(2, topics.size());
        assertTrue(topics.contains("CS 124"));
        assertTrue(topics.contains("MATH 231"));
    }

    @Test
    public void buildTopicList_caseVariants_deduplicatesIntoOne() {
        List<Post> all = Arrays.asList(postWithTopic("cs 124"), postWithTopic("CS 124"), postWithTopic("Cs 124"));
        List<String> topics = HomeActivity.buildTopicList(all);
        assertEquals(1, topics.size());
        assertEquals("CS 124", topics.get(0));
    }

    @Test
    public void buildTopicList_nullTopic_isIgnored() {
        List<Post> all = Arrays.asList(postWithTopic(null), postWithTopic("CS 124"));
        List<String> topics = HomeActivity.buildTopicList(all);
        assertEquals(1, topics.size());
        assertEquals("CS 124", topics.get(0));
    }

    @Test
    public void buildTopicList_emptyTopic_isIgnored() {
        List<Post> all = Arrays.asList(postWithTopic(""), postWithTopic("CS 124"));
        List<String> topics = HomeActivity.buildTopicList(all);
        assertEquals(1, topics.size());
        assertEquals("CS 124", topics.get(0));
    }

    @Test
    public void buildTopicList_emptyList_returnsEmptyList() {
        assertTrue(HomeActivity.buildTopicList(Collections.emptyList()).isEmpty());
    }
}
