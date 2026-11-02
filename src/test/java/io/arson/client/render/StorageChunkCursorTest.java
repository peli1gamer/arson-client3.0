package io.arson.client.render;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StorageChunkCursorTest {
    @Test
    void visitsEveryChunkExactlyOnce() {
        StorageChunkCursor cursor = new StorageChunkCursor(-2, 4, 2, 6);
        Set<String> visited = new HashSet<>();
        while (cursor.hasNext()) {
            visited.add(cursor.nextX() + ":" + cursor.nextZ());
            cursor.advance();
        }
        assertEquals(15, visited.size());
        assertEquals(15L, cursor.processed());
        assertEquals(cursor.total(), cursor.processed());
        assertTrue(visited.contains("-2:4"));
        assertTrue(visited.contains("2:6"));
    }

    @Test
    void processesLargeAreasInSmallBatches() {
        StorageChunkCursor cursor = new StorageChunkCursor(-8, -8, 8, 8);
        int frames = 0;
        long processed = 0L;
        while (cursor.hasNext()) {
            int budget = 0;
            while (cursor.hasNext() && budget < StorageScanner.MAX_CHUNKS_PER_FRAME) {
                cursor.nextX();
                cursor.nextZ();
                cursor.advance();
                budget++;
            }
            assertTrue(budget <= StorageScanner.MAX_CHUNKS_PER_FRAME);
            processed += budget;
            frames++;
        }
        assertEquals(289L, processed);
        assertTrue(frames > 1);
        assertTrue(frames <= Math.ceil(289.0 / StorageScanner.MAX_CHUNKS_PER_FRAME));
    }

    @Test
    void rejectsInvalidBoundsAndExhaustedReads() {
        assertThrows(IllegalArgumentException.class, () -> new StorageChunkCursor(2, 0, 1, 0));
        StorageChunkCursor cursor = new StorageChunkCursor(0, 0, 0, 0);
        cursor.advance();
        assertFalse(cursor.hasNext());
        assertThrows(IllegalStateException.class, cursor::nextX);
        assertThrows(IllegalStateException.class, cursor::nextZ);
    }
}