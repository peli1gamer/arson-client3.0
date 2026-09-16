package io.arson.client;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingModelTest {
    private enum Mode { FIRST, SECOND, THIRD }
    @Test void enumSettingCyclesAndWraps(){EnumSetting<Mode> setting=new EnumSetting<>("mode","Mode",Mode.FIRST);setting.cycle(1);assertEquals(Mode.SECOND,setting.get());setting.cycle(1);assertEquals(Mode.THIRD,setting.get());setting.cycle(1);assertEquals(Mode.FIRST,setting.get());setting.cycle(-1);assertEquals(Mode.THIRD,setting.get());}
    @Test void numericSettingProvidesClampedStepsAndNormalizedValue(){DoubleSetting setting=new DoubleSetting("amount","Amount",5,0,10,2.5);assertEquals(0.5,setting.normalized(),0.0001);setting.increment();assertEquals(7.5,setting.get());setting.increment();assertEquals(10,setting.get());setting.decrement();assertEquals(7.5,setting.get());setting.set(100.0);assertEquals(10,setting.get());}
    @Test void conditionalVisibilityAndDescriptionsAreObservable(){BooleanSetting gate=new BooleanSetting("gate","Gate",false);BooleanSetting dependent=new BooleanSetting("dependent","Dependent",true);dependent.description("Shown only while the gate is enabled.").visibleWhen(gate::enabled);assertFalse(dependent.visible());gate.set(true);assertTrue(dependent.visible());assertEquals("Shown only while the gate is enabled.",dependent.description());}
    @Test void breadthAddsBenignCoverageAcrossRequestedCategories(){ModuleManager manager=new ModuleManager();manager.registerDefaults();assertNotNull(manager.get("combat-status"));assertNotNull(manager.get("player-status"));assertNotNull(manager.get("movement-status"));assertNotNull(manager.get("render-profile"));assertNotNull(manager.get("world-status"));assertNotNull(manager.get("storage-status"));assertNotNull(manager.get("utility-status"));assertTrue(manager.categoryCount(Module.Category.COMBAT)>=2);assertTrue(manager.categoryCount(Module.Category.PLAYER)>=2);assertTrue(manager.categoryCount(Module.Category.WORLD)>=2);}
}
