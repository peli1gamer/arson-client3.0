package io.arson.client.module;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LiveTelemetryTrancheTest {
 @Test void defaultsAreSafe(){var input=new PlayerInputInfoModule();var weather=new WorldWeatherInfoModule();var viewport=new RenderViewportInfoModule();assertFalse(input.forward());assertFalse(input.jump());assertFalse(weather.raining());assertEquals(0f,weather.rainStrength());assertEquals(0,viewport.guiWidth());assertTrue(input.description().contains("input"));assertTrue(weather.description().contains("weather"));assertTrue(viewport.description().contains("viewport"));}
 @Test void managerContainsLiveTelemetry(){var m=new ModuleManager();m.registerDefaults();assertEquals(Module.Category.PLAYER,m.get("player-input-info").category());assertEquals(Module.Category.WORLD,m.get("world-weather-info").category());assertEquals(Module.Category.RENDER,m.get("render-viewport-info").category());assertTrue(m.search("weather").stream().anyMatch(x->x.id().equals("world-weather-info")));}
}
