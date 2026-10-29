package io.arson.client;

import io.arson.client.ui.HudSelectionModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HudSelectionModelTest {
    @Test void singleSelectionReplacesPrevious(){var s=new HudSelectionModel("watermark");s.select("fps",false);assertEquals(1,s.size());assertTrue(s.contains("fps"));assertFalse(s.contains("watermark"));}
    @Test void additiveSelectionTogglesMembership(){var s=new HudSelectionModel("watermark");s.select("fps",true);assertEquals(2,s.size());s.select("fps",true);assertEquals(1,s.size());assertTrue(s.contains("watermark"));}
    @Test void selectAllAndClearAreStable(){var s=new HudSelectionModel(null);s.replace(java.util.List.of("watermark","fps","coordinates"));assertEquals(3,s.size());s.clear();assertTrue(s.elements().isEmpty());}
    @Test void elementOrderIsStableForGroupOperations(){var s=new HudSelectionModel(null);s.replace(java.util.List.of("watermark","fps","coordinates"));assertEquals(java.util.List.of("watermark","fps","coordinates"),new java.util.ArrayList<>(s.elements()));assertEquals(java.util.List.of("watermark","fps","coordinates"),new java.util.ArrayList<>(s.elements()));}
    @Test void returnedElementsCannotMutateSelection(){var s=new HudSelectionModel("watermark");assertThrows(UnsupportedOperationException.class,()->s.elements().add("fps"));assertEquals(1,s.size());}
}
