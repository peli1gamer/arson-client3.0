package io.arson.client;

import io.arson.client.module.Module;
import io.arson.client.settings.BooleanSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleHelpTest {
    @Test void helpIncludesDescriptionAndCategory() {
        Module module = new Module("sample", "Sample", Module.Category.MISC, "Explains the local diagnostic view.") {
            { setting(new BooleanSetting("enabled", "Enabled", true)); }
        };
        assertEquals("Explains the local diagnostic view.", module.description());
        assertEquals("Sample [Misc]: Explains the local diagnostic view.", module.help());
    }
}
