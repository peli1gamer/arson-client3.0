package io.arson.client.module;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AdditionalLiveTelemetryTest {
 @Test void newTelemetryStartsSafe(){var pose=new PlayerPoseInfoModule();var chunk=new WorldChunkInfoModule();var camera=new RenderCameraInfoModule();assertEquals(Module.Category.PLAYER,pose.category());assertEquals("STANDING",pose.pose());assertEquals(0.0,pose.speed());assertEquals(Module.Category.WORLD,chunk.category());assertEquals(0,chunk.chunkX());assertEquals(0,chunk.chunkZ());assertEquals(Module.Category.RENDER,camera.category());assertEquals("FIRST_PERSON",camera.type());assertEquals(0.0,camera.distance());}
 @Test void managerContainsNewTelemetry(){var m=new ModuleManager();m.registerDefaults();assertNotNull(m.get("player-pose-info"));assertNotNull(m.get("world-chunk-info"));assertNotNull(m.get("render-camera-info"));assertEquals(Module.Category.PLAYER,m.get("player-pose-info").category());assertEquals(Module.Category.WORLD,m.get("world-chunk-info").category());assertEquals(Module.Category.RENDER,m.get("render-camera-info").category());}
}