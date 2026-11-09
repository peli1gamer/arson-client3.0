package io.arson.client.module;

import meteordevelopment.meteorclient.utils.misc.Keybind;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.*;

class MeteorKeybindTest {
    @Test
    void supportsKeyMouseAndModifierBindings() {
        Keybind plain = Keybind.fromKey(GLFW.GLFW_KEY_R);
        assertTrue(plain.isSet());
        assertTrue(plain.isKey());
        assertTrue(plain.matches(true, GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_SHIFT));
        assertFalse(plain.matches(false, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 0));

        Keybind modified = Keybind.fromKeys(GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT);
        assertTrue(modified.matches(true, GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT));
        assertFalse(modified.matches(true, GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_CONTROL));

        Keybind mouse = Keybind.fromButton(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        assertFalse(mouse.isKey());
        assertTrue(mouse.matches(false, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 0));
    }

    @Test
    void rejectsMeteorReservedBindingInputs() {
        Keybind none = Keybind.none();
        assertFalse(none.isSet());
        assertFalse(none.canBindTo(true, GLFW.GLFW_KEY_UNKNOWN, 0));
        assertFalse(none.canBindTo(true, GLFW.GLFW_KEY_ESCAPE, 0));
        assertFalse(none.canBindTo(false, GLFW.GLFW_MOUSE_BUTTON_LEFT, 0));
        assertFalse(none.canBindTo(false, GLFW.GLFW_MOUSE_BUTTON_RIGHT, 0));
        assertTrue(none.canBindTo(true, GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_CONTROL));
    }

    @Test
    void arsonModuleLegacyKeyCodeApiUsesMeteorKeybindStorage() {
        TestModule module = new TestModule();
        module.setKeyCode(GLFW.GLFW_KEY_R);
        assertEquals(GLFW.GLFW_KEY_R, module.keyCode());
        assertTrue(module.hasKeybind());

        module.setKeybind(Keybind.fromKeys(GLFW.GLFW_KEY_T, GLFW.GLFW_MOD_ALT));
        assertEquals(GLFW.GLFW_KEY_T, module.keyCode());
        assertEquals(GLFW.GLFW_MOD_ALT, module.keybind().getModifiers());

        module.setKeyCode(0);
        assertFalse(module.hasKeybind());
    }

    private static final class TestModule extends Module {
        private TestModule() {
            super("keybind-test", "Keybind Test", Category.MISC);
        }
    }
}
