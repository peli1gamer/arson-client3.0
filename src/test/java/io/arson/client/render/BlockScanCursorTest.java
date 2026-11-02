package io.arson.client.render;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockScanCursorTest {
    @Test
    void visitsEveryPositionExactlyOnce() {
        BlockScanCursor cursor = new BlockScanCursor(-1, 2, 4, 1, 3, 5);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<BlockPos> visited = new ArrayList<>();
        while (cursor.hasNext()) {
            cursor.setPosition(mutable);
            visited.add(mutable.immutable());
            cursor.advance();
        }
        assertEquals(12, visited.size());
        assertEquals(12L, cursor.processed());
        assertEquals(cursor.total(), cursor.processed());
        assertEquals(visited.size(), visited.stream().distinct().count());
        assertTrue(visited.contains(new BlockPos(-1, 2, 4)));
        assertTrue(visited.contains(new BlockPos(1, 3, 5)));
    }

    @Test
    void rejectsInvalidBounds() {
        assertThrows(IllegalArgumentException.class, () -> new BlockScanCursor(2, 0, 0, 1, 0, 0));
    }

    @Test
    void exhaustedCursorCannotBeRead() {
        BlockScanCursor cursor = new BlockScanCursor(0, 0, 0, 0, 0, 0);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        cursor.setPosition(mutable);
        cursor.advance();
        assertFalse(cursor.hasNext());
        assertThrows(IllegalStateException.class, () -> cursor.setPosition(mutable));
        cursor.advance();
        assertEquals(1L, cursor.processed());
    }

    @Test
    void maximumConfiguredRangeCanBeProcessedInBoundedBatches() {
        int radius = 64;
        BlockScanCursor cursor = new BlockScanCursor(-radius, -radius, -radius,
                radius, radius, radius);
        long expectedTotal = 129L * 129L * 129L;
        long processed = 0L;
        int frames = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        while (cursor.hasNext()) {
            int frameBudget = 0;
            while (cursor.hasNext() && frameBudget < BlockScanner.MAX_BLOCKS_PER_FRAME) {
                cursor.setPosition(mutable);
                cursor.advance();
                frameBudget++;
            }
            assertTrue(frameBudget <= BlockScanner.MAX_BLOCKS_PER_FRAME);
            processed += frameBudget;
            frames++;
        }

        assertEquals(expectedTotal, processed);
        assertEquals(expectedTotal, cursor.processed());
        assertTrue(frames > 1);
        assertTrue(frames <= Math.ceil(expectedTotal / (double) BlockScanner.MAX_BLOCKS_PER_FRAME));
    }
}