package io.arson.client;

import io.arson.client.notification.NotificationCenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationCenterTest {
    @AfterEach
    void clear() { NotificationCenter.clear(); }

    @Test
    void pushesActiveNotificationAndExpiresIt() {
        long now = System.currentTimeMillis();
        NotificationCenter.push("Sprint", "Enabled", 1000);
        assertEquals(1, NotificationCenter.active(now).size());
        assertEquals("Sprint", NotificationCenter.active(now).get(0).title());
        assertTrue(NotificationCenter.active(now).get(0).activeAt(now));
        assertTrue(NotificationCenter.active(now + 1001).isEmpty());
    }

    @Test
    void queueIsBoundedAndKeepsNewestNotifications() {
        for (int i = 0; i < 7; i++) NotificationCenter.push("N" + i, "message", 5000);
        assertEquals(4, NotificationCenter.active(System.currentTimeMillis()).size());
        assertEquals("N3", NotificationCenter.active(System.currentTimeMillis()).get(0).title());
        assertEquals("N6", NotificationCenter.active(System.currentTimeMillis()).get(3).title());
    }
}
