package io.arson.client.render;

import com.arson.client.render.RenderColor;
import com.arson.client.render.RenderStyle;
import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageRenderProfile;
import com.arson.client.render.StorageType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageOverlayTest {
    @Test
    void rangeUsesTargetCentreInsteadOfMinimumCorner() {
        StorageRenderProfile profile = new StorageRenderProfile();
        StorageOverlay overlay = new StorageOverlay(profile);
        overlay.range(10.0);
        StorageOverlay.StorageTarget target =
                new StorageOverlay.StorageTarget(StorageType.CHEST, 9.8, 0, 0, 1, 1, 1);
        RenderStyle style = new RenderStyle(new RenderColor(255, 190, 70, 255),
                true, true, 0.25f, 1.0f, 1.0f);

        List<com.arson.client.render.RenderBox> boxes = overlay.build(0, 0, 0,
                List.of(target), type -> style);

        assertEquals(0, boxes.size());
    }

    @Test
    void excludesDisabledStorageTypes() {
        StorageRenderProfile profile = new StorageRenderProfile();
        profile.enabled(StorageType.CHEST, false);
        StorageOverlay overlay = new StorageOverlay(profile);
        overlay.range(10.0);
        StorageOverlay.StorageTarget target =
                new StorageOverlay.StorageTarget(StorageType.CHEST, 1, 0, 0, 1, 1, 1);
        RenderStyle style = new RenderStyle(new RenderColor(255, 190, 70, 255),
                true, true, 0.25f, 1.0f, 1.0f);

        assertEquals(0, overlay.build(0, 0, 0, List.of(target), type -> style).size());
    }
}