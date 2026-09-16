package io.arson.client.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Small client-side notification queue. It stores state only; rendering is handled by the HUD layer. */
public final class NotificationCenter {
    public record Notification(String title, String message, long expiresAt) {
        public boolean activeAt(long now) { return now < expiresAt; }
    }

    private static final List<Notification> NOTIFICATIONS = new ArrayList<>();
    private static final int MAX_NOTIFICATIONS = 4;
    private static final long DEFAULT_DURATION_MS = 2200L;

    private NotificationCenter() {}

    public static void push(String title, String message) {
        push(title, message, DEFAULT_DURATION_MS);
    }

    public static synchronized void push(String title, String message, long durationMs) {
        long duration = Math.max(250L, durationMs);
        prune(System.currentTimeMillis());
        NOTIFICATIONS.add(new Notification(title == null ? "Arson" : title,
                message == null ? "" : message, System.currentTimeMillis() + duration));
        while (NOTIFICATIONS.size() > MAX_NOTIFICATIONS) NOTIFICATIONS.remove(0);
    }

    public static synchronized List<Notification> active(long now) {
        prune(now);
        return List.copyOf(NOTIFICATIONS);
    }

    public static synchronized void clear() {
        NOTIFICATIONS.clear();
    }

    static synchronized void prune(long now) {
        Iterator<Notification> iterator = NOTIFICATIONS.iterator();
        while (iterator.hasNext()) if (!iterator.next().activeAt(now)) iterator.remove();
    }
}
