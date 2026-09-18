package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;

/** Live client-side performance/runtime telemetry for HUD and API consumers. */
public final class ClientPerformanceInfoModule extends Module {
    private final BooleanSetting showMemory = setting(new BooleanSetting("show-memory", "Show Memory", true));
    private final BooleanSetting showJava = setting(new BooleanSetting("show-java", "Show Java", false));
    private int fps;
    private long usedMemoryMb;
    private long maxMemoryMb;
    private String javaVersion = "";

    public ClientPerformanceInfoModule() {
        super("client-performance-info", "Client Performance Info", Category.MISC,
            "Reports live FPS and JVM memory usage without changing client performance settings.");
    }

    @Override
    protected void onTick(Minecraft client) {
        fps = client.getFps();
        Runtime runtime = Runtime.getRuntime();
        usedMemoryMb = Math.max(0L, (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L));
        maxMemoryMb = Math.max(0L, runtime.maxMemory() / (1024L * 1024L));
        javaVersion = System.getProperty("java.version", "");
    }

    public int fps() { return fps; }
    public long usedMemoryMb() { return usedMemoryMb; }
    public long maxMemoryMb() { return maxMemoryMb; }
    public String javaVersion() { return javaVersion; }
    public boolean showMemory() { return showMemory.enabled(); }
    public boolean showJava() { return showJava.enabled(); }

    public String formatted() {
        StringBuilder out = new StringBuilder("FPS ").append(fps);
        if (showMemory()) out.append("  Memory ").append(usedMemoryMb).append("/").append(maxMemoryMb).append(" MB");
        if (showJava() && !javaVersion.isBlank()) out.append("  Java ").append(javaVersion);
        return out.toString();
    }
}
