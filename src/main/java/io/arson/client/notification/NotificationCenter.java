package io.arson.client.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NotificationCenter {
    public enum Priority { LOW, NORMAL, HIGH }
    public enum Position { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public record Notification(String title, String message, long createdAt, long expiresAt, Priority priority) {
        public Notification(String title, String message, long expiresAt) { this(title, message, expiresAt - 2200L, expiresAt, Priority.NORMAL); }
        public boolean activeAt(long now) { return now < expiresAt; }
        public float progress(long now) { if (expiresAt <= createdAt) return 0f; return Math.max(0f, Math.min(1f, (expiresAt - now) / (float)(expiresAt - createdAt))); }
    }
    private static final List<Notification> NOTIFICATIONS = new ArrayList<>();
    private static final int MAX_NOTIFICATIONS = 5;
    private static final long DEFAULT_DURATION_MS = 2200L;
    private static volatile long durationMs = DEFAULT_DURATION_MS;
    private static volatile Position position = Position.TOP_RIGHT;
    private NotificationCenter() {}
    public static void push(String title, String message) { push(title, message, durationMs, Priority.NORMAL); }
    public static void push(String title, String message, long duration) { push(title, message, duration, Priority.NORMAL); }
    public static synchronized void push(String title, String message, long duration, Priority priority) {
        long now = System.currentTimeMillis(); long safe = Math.max(250L, duration); prune(now);
        NOTIFICATIONS.add(new Notification(title == null ? "Arson" : title, message == null ? "" : message, now, now + safe, priority == null ? Priority.NORMAL : priority));
        NOTIFICATIONS.sort((a,b) -> Integer.compare(b.priority().ordinal(), a.priority().ordinal()));
        while (NOTIFICATIONS.size() > MAX_NOTIFICATIONS) NOTIFICATIONS.remove(NOTIFICATIONS.size() - 1);
    }
    public static synchronized List<Notification> active(long now) { prune(now); return List.copyOf(NOTIFICATIONS); }
    public static void configure(long duration, Position newPosition) { durationMs = Math.max(250L, duration); if (newPosition != null) position = newPosition; }
    public static long durationMs() { return durationMs; }
    public static Position position() { return position; }
    public static synchronized void clear() { NOTIFICATIONS.clear(); }
    static synchronized void prune(long now) { Iterator<Notification> iterator = NOTIFICATIONS.iterator(); while (iterator.hasNext()) if (!iterator.next().activeAt(now)) iterator.remove(); }
}
