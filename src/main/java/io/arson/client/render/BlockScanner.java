package io.arson.client.render;

import io.arson.client.module.BlockESPModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Cached block discovery for Block ESP. Only loaded-world state is queried. */
public final class BlockScanner {
    public List<BlockTarget> scan(Minecraft client, BlockESPModule module) {
        if (client.level == null || client.player == null || !module.enabled()) return List.of();

        int radius = Math.max(1, Math.min(64, (int) Math.round(module.range())));
        double rangeSquared = module.range() * module.range();
        BlockPos center = client.player.blockPosition();
        List<BlockTarget> result = new ArrayList<>();

        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                if (y < client.level.getMinY() || y > client.level.getMaxY()) continue;
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    double dx = x + 0.5 - client.player.getX();
                    double dy = y + 0.5 - client.player.getY();
                    double dz = z + 0.5 - client.player.getZ();
                    if (dx * dx + dy * dy + dz * dz > rangeSquared) continue;

                    BlockState state = client.level.getBlockState(new BlockPos(x, y, z));
                    RenderStyleMatch match = match(state.getBlock(), module);
                    if (match == null) continue;
                    result.add(new BlockTarget(x, y, z, x + 1.0, y + 1.0, z + 1.0,
                            match.style(), Math.sqrt(dx * dx + dy * dy + dz * dz)));
                }
            }
        }
        return List.copyOf(result);
    }

    private static RenderStyleMatch match(Block block, BlockESPModule module) {
        if (module.showDiamond() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE))
            return new RenderStyleMatch(module.diamondStyle());
        if (module.showEmerald() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE))
            return new RenderStyleMatch(module.emeraldStyle());
        if (module.showGold() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE))
            return new RenderStyleMatch(module.goldStyle());
        if (module.showIron() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE))
            return new RenderStyleMatch(module.ironStyle());
        if (module.showCopper() && (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE))
            return new RenderStyleMatch(module.copperStyle());
        if (module.showCoal() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE))
            return new RenderStyleMatch(module.coalStyle());
        if (module.showRedstone() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE))
            return new RenderStyleMatch(module.redstoneStyle());
        if (module.showLapis() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE))
            return new RenderStyleMatch(module.lapisStyle());
        if (module.showQuartz() && block == Blocks.NETHER_QUARTZ_ORE)
            return new RenderStyleMatch(module.quartzStyle());
        if (module.showAncientDebris() && block == Blocks.ANCIENT_DEBRIS)
            return new RenderStyleMatch(module.ancientDebrisStyle());
        return null;
    }

    private record RenderStyleMatch(com.arson.client.render.RenderStyle style) {}
}
