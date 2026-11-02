package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerInventorySlotMapperTest {
    @Test
    void mapsHotbarAndMainInventorySlotsToMenuIds() {
        assertEquals(36, PlayerInventorySlotMapper.menuSlotId(0));
        assertEquals(44, PlayerInventorySlotMapper.menuSlotId(8));
        assertEquals(9, PlayerInventorySlotMapper.menuSlotId(9));
        assertEquals(35, PlayerInventorySlotMapper.menuSlotId(35));
    }

    @Test
    void mapsArmorAndOffhandSlotsToMenuIds() {
        assertEquals(8, PlayerInventorySlotMapper.menuSlotId(36));
        assertEquals(5, PlayerInventorySlotMapper.menuSlotId(39));
        assertEquals(45, PlayerInventorySlotMapper.menuSlotId(40));
    }

    @Test
    void rejectsInvalidInventoryIndices() {
        assertEquals(-1, PlayerInventorySlotMapper.menuSlotId(-1));
        assertEquals(-1, PlayerInventorySlotMapper.menuSlotId(41));
    }
}
