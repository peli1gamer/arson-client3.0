package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderColor;
import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageRenderStageTest {
    @Test
    void highContrastStrengthensVisibleStorageStyles() {
        var original = new RenderStyle(new RenderColor(40, 80, 120, 150), true, true, 0.2f, 0.5f, 1.0f);
        var box = new RenderBox(0, 0, 0, 1, 1, 1, original);

        var styled = StorageRenderStage.applyRenderMode(List.of(box), ModuleManager.RenderMode.HIGH_CONTRAST).get(0).style();

        assertTrue(styled.filled());
        assertTrue(styled.outline());
        assertTrue(styled.fillAlpha() >= 0.55f);
        assertTrue(styled.outlineAlpha() >= 0.95f);
        assertTrue(styled.lineWidth() >= 2.0f);
    }

    @Test
    void minimalProfileUsesOutlineOnly() {
        var original = new RenderStyle(new RenderColor(40, 80, 120, 150), true, true, 0.2f, 0.5f, 1.0f);
        var box = new RenderBox(0, 0, 0, 1, 1, 1, original);

        var styled = StorageRenderStage.applyRenderMode(List.of(box), ModuleManager.RenderMode.MINIMAL).get(0).style();

        assertFalse(styled.filled());
        assertTrue(styled.outline());
        assertEquals(0.0f, styled.fillAlpha());
    }

    @Test
    void storageCacheRefreshesImmediatelyForWorldAndRenderDistanceChanges() {
        Object level = new Object();
        assertFalse(StorageRenderStage.shouldRescan(40, 20, 100, 3, 3, level, level, 12, 12));
        assertTrue(StorageRenderStage.shouldRescan(40, 20, 100, 3, 3, level, level, 16, 12));
        assertTrue(StorageRenderStage.shouldRescan(40, 20, 100, 3, 3, new Object(), level, 12, 12));
        assertTrue(StorageRenderStage.shouldRescan(40, 20, 100, 4, 3, level, level, 12, 12));
        assertTrue(StorageRenderStage.shouldRescan(120, 20, 100, 3, 3, level, level, 12, 12));
        assertTrue(StorageRenderStage.shouldRescan(10, 20, 100, 3, 3, level, level, 12, 12));
    }

    @Test
    void clusterCacheRefreshesForPresentationSettingsWithoutWorldRescan() {
        var standard = new StorageRenderStage.ClusterSettings(true, true, true, false,
                true, false, 8.0);
        assertFalse(StorageRenderStage.shouldRefreshClusters(standard, standard, 4, 4));
        assertTrue(StorageRenderStage.shouldRefreshClusters(standard, standard, 5, 4));

        var changedRadius = new StorageRenderStage.ClusterSettings(true, true, true, false,
                true, false, 12.0);
        assertTrue(StorageRenderStage.shouldRefreshClusters(changedRadius, standard, 4, 4));
        var disabled = new StorageRenderStage.ClusterSettings(false, true, true, false,
                true, false, 8.0);
        assertTrue(StorageRenderStage.shouldRefreshClusters(disabled, standard, 4, 4));
        assertTrue(standard.includes(com.arson.client.render.StorageType.CHEST));
        assertFalse(standard.includes(com.arson.client.render.StorageType.SHULKER));
        assertFalse(disabled.includes(com.arson.client.render.StorageType.CHEST));
    }
}
