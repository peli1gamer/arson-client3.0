package io.arson.client;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.IntSetting;
import io.arson.client.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SettingModelTest {
    private enum Mode { FIRST, SECOND, THIRD }

    @Test void enumSettingCyclesAndWraps(){EnumSetting<Mode> setting=new EnumSetting<>("mode","Mode",Mode.FIRST);setting.cycle(1);assertEquals(Mode.SECOND,setting.get());setting.cycle(1);assertEquals(Mode.THIRD,setting.get());setting.cycle(1);assertEquals(Mode.FIRST,setting.get());setting.cycle(-1);assertEquals(Mode.THIRD,setting.get());}
    @Test void numericSettingProvidesClampedStepsAndNormalizedValue(){DoubleSetting setting=new DoubleSetting("amount","Amount",5,0,10,2.5);assertEquals(0.5,setting.normalized(),0.0001);setting.increment();assertEquals(7.5,setting.get());setting.increment();assertEquals(10,setting.get());setting.decrement();assertEquals(7.5,setting.get());setting.set(100.0);assertEquals(10,setting.get());}
    @Test void integerSettingClampsSnapsAndNormalizes() {
        IntSetting setting = new IntSetting("count", "Count", 4, 0, 20, 2);
        assertEquals(0.2, setting.normalized(), 0.0001);
        setting.increment();
        assertEquals(6, setting.get());
        setting.set(100);
        assertEquals(20, setting.get());
        setting.decrement();
        assertEquals(18, setting.get());
        setting.set(-10);
        assertEquals(0, setting.get());
    }
    @Test void conditionalVisibilityAndDescriptionsAreObservable(){
        BooleanSetting gate=new BooleanSetting("gate","Gate",false);
        BooleanSetting dependent=new BooleanSetting("dependent","Dependent",true);
        dependent.description("Shown only while the gate is enabled.").visibleWhen(gate::enabled);
        assertFalse(dependent.visible());
        assertFalse(dependent.isVisible());
        gate.set(true);
        assertTrue(dependent.visible());
        assertTrue(dependent.isVisible());
        assertEquals("Shown only while the gate is enabled.",dependent.description());
        assertEquals("Shown only while the gate is enabled.",dependent.description);
    }
    @Test void settingCallbacksRunForChangesResetsAndModuleActivation() {
        BooleanSetting setting = new BooleanSetting("callback", "Callback", false);
        List<Boolean> changed = new ArrayList<>();
        List<String> activated = new ArrayList<>();
        setting.onChanged(changed::add);
        setting.onModuleActivated(value -> activated.add(value.id()));
        setting.set(true);
        setting.reset();
        setting.onActivated();
        assertEquals(List.of(true, false), changed);
        assertEquals(List.of("callback"), activated);
    }
    @Test void meteorSettingsPersistenceUsesStableSettingAndGroupIds() {
        BooleanSetting sourceSetting = new BooleanSetting("stable-setting-id", "Old Display Name", false);
        SettingGroup sourceGroup = new SettingGroup("stable-group-id", "Old Group Name", "test");
        sourceSetting.group(sourceGroup);
        sourceSetting.set(true);
        sourceGroup.sectionExpanded = false;
        Settings source = new Settings();
        source.addGroup(sourceGroup);
        NbtCompound saved = source.toTag();

        BooleanSetting loadedSetting = new BooleanSetting("stable-setting-id", "Renamed Display Name", false);
        SettingGroup loadedGroup = new SettingGroup("stable-group-id", "Renamed Group Name", "test");
        loadedSetting.group(loadedGroup);
        Settings loaded = new Settings();
        loaded.addGroup(loadedGroup);
        loaded.fromTag(saved);

        assertTrue(loadedSetting.get());
        assertFalse(loadedGroup.sectionExpanded);
    }
    @Test void failedGroupRegistrationDoesNotLeavePartialMembership() {
        SettingGroup group = new SettingGroup("group", "Group", "test");
        group.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Collision").build());

        BooleanSetting rejected = new BooleanSetting("collision", "Collision", false);
        assertThrows(IllegalArgumentException.class, () -> rejected.group(group));
        assertNull(rejected.group());
        assertTrue(group.settings().isEmpty());
    }

    @Test void settingCannotBeSilentlyReparented() {
        SettingGroup first = new SettingGroup("first", "First", "test");
        SettingGroup second = new SettingGroup("second", "Second", "test");
        BooleanSetting setting = new BooleanSetting("unique", "Unique", false);

        setting.group(first);
        assertThrows(IllegalStateException.class, () -> setting.group(second));
        assertSame(first, setting.group());
        assertTrue(first.settings().contains(setting));
        assertTrue(second.settings().isEmpty());
    }

    @Test void breadthAddsBenignCoverageAcrossRequestedCategories(){ModuleManager manager=new ModuleManager();manager.registerDefaults();assertNotNull(manager.get("combat-status"));assertNotNull(manager.get("player-status"));assertNotNull(manager.get("movement-status"));assertNotNull(manager.get("render-profile"));assertNotNull(manager.get("world-status"));assertNotNull(manager.get("storage-status"));assertNotNull(manager.get("utility-status"));assertTrue(manager.categoryCount(Module.Category.COMBAT)>=2);assertTrue(manager.categoryCount(Module.Category.PLAYER)>=2);assertTrue(manager.categoryCount(Module.Category.WORLD)>=2);}
}
