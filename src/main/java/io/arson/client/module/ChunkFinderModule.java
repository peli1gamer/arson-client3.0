package io.arson.client.module;
import io.arson.client.settings.DoubleSetting;
public final class ChunkFinderModule extends StructureESPModule {
 private final DoubleSetting score=setting(new DoubleSetting("minimum-score","Minimum Pattern Score",8,1,40,1));
 public ChunkFinderModule(){super("chunk-finder","Chunk Finder",Detector.CHUNK_FINDER);}
 public int minimumScore(){return (int)Math.round(score.get());}
}