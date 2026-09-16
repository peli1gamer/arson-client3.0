package io.arson.client;

import io.arson.client.notification.NotificationCenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationCenterTest {
    @AfterEach void clear(){NotificationCenter.clear();NotificationCenter.configure(2200,NotificationCenter.Position.TOP_RIGHT);}
    @Test void pushesActiveNotificationAndExpiresIt(){long now=System.currentTimeMillis();NotificationCenter.push("Sprint","Enabled",1000);assertEquals(1,NotificationCenter.active(now).size());assertEquals("Sprint",NotificationCenter.active(now).get(0).title());assertTrue(NotificationCenter.active(now).get(0).activeAt(now));assertTrue(NotificationCenter.active(now+1001).isEmpty());}
    @Test void queueIsBoundedAndKeepsHighPriority(){for(int i=0;i<4;i++)NotificationCenter.push("N"+i,"message",5000);NotificationCenter.push("urgent","message",5000,NotificationCenter.Priority.HIGH);assertEquals(5,NotificationCenter.active(System.currentTimeMillis()).size());assertEquals("urgent",NotificationCenter.active(System.currentTimeMillis()).get(0).title());}
    @Test void progressAndConfigurationAreDeterministic(){long before=System.currentTimeMillis();NotificationCenter.configure(4000,NotificationCenter.Position.BOTTOM_LEFT);NotificationCenter.push("x","y");var n=NotificationCenter.active(before+1).get(0);assertEquals(NotificationCenter.Position.BOTTOM_LEFT,NotificationCenter.position());assertEquals(4000,NotificationCenter.durationMs());assertTrue(n.progress(before+1)>0.9f);assertTrue(n.progress(n.expiresAt()+1)==0f);}
}
