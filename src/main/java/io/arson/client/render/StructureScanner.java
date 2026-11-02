package io.arson.client.render;

import io.arson.client.module.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Shared bounded world snapshot for specialized ESP. All Minecraft world reads occur
 * on the render/client thread; detection runs only against the captured immutable-by-convention snapshot.
 */
public final class StructureScanner {
    static final int MAX_POSITIONS_PER_FRAME = 8_192;
    private Object levelIdentity;
    private Object playerIdentity;
    private BlockPos origin;
    private int radius;
    private int cursorX, cursorY, cursorZ;
    private long lastCompleteTick = Long.MIN_VALUE;
    private int moduleSignature;
    private Map<Long, BlockState> snapshot = Map.of();
    private Map<Long, BlockState> working;
    private List<StructureTarget> targets = List.of();

    public void invalidate() {
        levelIdentity = null; playerIdentity = null; origin = null; radius = 0;
        lastCompleteTick = Long.MIN_VALUE; moduleSignature = 0;
        snapshot = Map.of(); working = null; targets = List.of();
    }

    public List<StructureTarget> scan(Minecraft client, List<StructureESPModule> modules) {
        if (client.level == null || client.player == null || modules.stream().noneMatch(Module::enabled)) {
            invalidate();
            return List.of();
        }
        int enabledSignature = signature(modules);
        BlockPos playerPos = client.player.blockPosition();
        int nextRadius = (int)Math.ceil(modules.stream().filter(Module::enabled)
                .mapToDouble(StructureESPModule::range).max().orElse(24.0));
        long tick = client.level.getGameTime();
        boolean identityChanged = levelIdentity != client.level || playerIdentity != client.player;
        boolean moved = origin == null || origin.distSqr(playerPos) >= 64.0;
        boolean intervalElapsed = lastCompleteTick == Long.MIN_VALUE || tick < lastCompleteTick
                || tick - lastCompleteTick >= modules.stream().filter(Module::enabled)
                .mapToInt(StructureESPModule::scanInterval).min().orElse(10);
        if (identityChanged || enabledSignature != moduleSignature || moved || intervalElapsed) {
            if (working == null) begin(client, playerPos, nextRadius, enabledSignature);
        }
        if (working != null) advance(client, modules, tick);
        return targets;
    }

    private void begin(Minecraft client, BlockPos playerPos, int scanRadius, int signature) {
        levelIdentity = client.level; playerIdentity = client.player;
        origin = playerPos.immutable(); radius = Math.max(8, Math.min(48, scanRadius));
        cursorX = origin.getX() - radius; cursorY = Math.max(client.level.getMinY(), origin.getY() - radius);
        cursorZ = origin.getZ() - radius;
        working = new HashMap<>((2 * radius + 1) * (2 * radius + 1));
        moduleSignature = signature;
    }

    private void advance(Minecraft client, List<StructureESPModule> modules, long tick) {
        int processed = 0;
        int maxY = Math.min(client.level.getMaxY() - 1, origin.getY() + radius);
        while (processed < MAX_POSITIONS_PER_FRAME) {
            if (cursorY > maxY) break;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(cursorX, cursorY, cursorZ);
            if (client.level.hasChunkAt(pos)) working.put(pos.asLong(), client.level.getBlockState(pos));
            processed++;
            cursorZ++;
            if (cursorZ > origin.getZ() + radius) { cursorZ = origin.getZ() - radius; cursorX++; }
            if (cursorX > origin.getX() + radius) { cursorX = origin.getX() - radius; cursorY++; }
        }
        if (cursorY > maxY) {
            snapshot = Map.copyOf(working);
            working = null;
            targets = detect(client, modules, snapshot);
            lastCompleteTick = tick;
        }
    }

    private static List<StructureTarget> detect(Minecraft client, List<StructureESPModule> modules,
                                                 Map<Long, BlockState> blocks) {
        Map<StructureESPModule.Detector, StructureESPModule> enabled = new EnumMap<>(StructureESPModule.Detector.class);
        for (StructureESPModule module : modules) if (module.enabled()) enabled.put(module.detector(), module);
        List<StructureTarget> found = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Set<Long> emitted = new HashSet<>();

        StructureESPModule holesBase = enabled.get(StructureESPModule.Detector.HOLE_TUNNEL_STAIRS);
        StructureESPModule covered = enabled.get(StructureESPModule.Detector.COVERED_HOLE);
        StructureESPModule oneByOne = enabled.get(StructureESPModule.Detector.ONE_BY_ONE_HOLE);
        StructureESPModule dripstone = enabled.get(StructureESPModule.Detector.DRIPSTONE);
        StructureESPModule beehives = enabled.get(StructureESPModule.Detector.BEEHIVE);

        for (Map.Entry<Long, BlockState> entry : blocks.entrySet()) {
            long key = entry.getKey();
            BlockState state = entry.getValue();
            BlockPos p = BlockPos.of(key);
            if (state.isAir()) {
                if (oneByOne != null && enclosedAir(blocks, p)) {
                    add(found, emitted, StructureESPModule.Detector.ONE_BY_ONE_HOLE, p.getX(), p.getY(), p.getZ(), 1, 1, 1);
                }
                if ((holesBase != null && holesBase.holes()) || covered != null) {
                    int depth = verticalAirDepth(blocks, p, 16);
                    boolean walls = cardinalWalls(blocks, p);
                    if (walls && depth >= (holesBase == null ? 4 : holesBase.minimumHoleDepth())) {
                        if (covered != null && solid(blocks, p.above())) {
                            BlockState cap = get(blocks, p.above());
                            if (!covered.playerPlacedOnly() || likelyPlacedCover(cap)) {
                                add(found, emitted, StructureESPModule.Detector.COVERED_HOLE,
                                        p.getX(), p.getY(), p.getZ(), 1, depth, 1);
                            }
                        } else if (holesBase != null && holesBase.holes()) {
                            add(found, emitted, StructureESPModule.Detector.HOLE_TUNNEL_STAIRS,
                                    p.getX(), p.getY(), p.getZ(), 1, depth, 1);
                        }
                    }
                }
                if (holesBase != null && holesBase.tunnels()) {
                    detectTunnel(blocks, p, (HoleTunnelStairsESPModule) holesBase, found, emitted);
                }
            }
            if (dripstone != null && state.is(Blocks.POINTED_DRIPSTONE)) {
                detectDripstone(blocks, p, dripstone, found, emitted);
            }
            if (beehives != null && (state.is(Blocks.BEEHIVE) || state.is(Blocks.BEE_NEST))) {
                add(found, emitted, StructureESPModule.Detector.BEEHIVE,
                        p.getX(), p.getY(), p.getZ(), 1, 1, 1);
            }
        }

        if (holesBase != null && holesBase.stairs()) detectStairs(blocks, holesBase, found, emitted);
        StructureESPModule chunkFinder = enabled.get(StructureESPModule.Detector.CHUNK_FINDER);
        if (chunkFinder instanceof ChunkFinderModule finder) detectChunks(blocks, finder, found, emitted, client);

        return List.copyOf(found);
    }

    private static void detectTunnel(Map<Long, BlockState> blocks, BlockPos start,
                                     HoleTunnelStairsESPModule module, List<StructureTarget> out, Set<Long> emitted) {
        for (int[] direction : new int[][]{{1,0},{0,1},{1,1},{1,-1}}) {
            boolean diagonal = direction[0] != 0 && direction[1] != 0;
            if (diagonal && !module.diagonalTunnels()) continue;
            int length = module.minimumTunnelLength();
            boolean valid = true;
            for (int i=0;i<length;i++) {
                BlockPos a = start.offset(direction[0]*i,0,direction[1]*i);
                if (!passable(blocks,a) || !passable(blocks,a.above())
                        || !solid(blocks,a.below()) || !solid(blocks,a.above(2))) { valid=false; break; }
            }
            if (!valid) continue;
            BlockPos before = start.offset(-direction[0],0,-direction[1]);
            if (passable(blocks,before) && passable(blocks,before.above())) continue;
            add(out,emitted,StructureESPModule.Detector.HOLE_TUNNEL_STAIRS,
                    start.getX(),start.getY(),start.getZ(),
                    Math.max(1,length*Math.abs(direction[0])),2,Math.max(1,length*Math.abs(direction[1])));
        }
    }

    private static void detectStairs(Map<Long,BlockState> blocks, HoleTunnelStairsESPModule module,
                                     List<StructureTarget> out, Set<Long> emitted) {
        for (Map.Entry<Long,BlockState> e:blocks.entrySet()) {
            if (!(e.getValue().getBlock() instanceof StairBlock)) continue;
            BlockPos start=BlockPos.of(e.getKey());
            for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
                int count=1;
                while(count<module.minimumStairLength()){
                    BlockPos next=start.offset(d[0]*count,count,d[1]*count);
                    BlockState s=get(blocks,next);
                    if(s==null || !(s.getBlock() instanceof StairBlock)) break;
                    count++;
                }
                if(count>=module.minimumStairLength())
                    add(out,emitted,StructureESPModule.Detector.HOLE_TUNNEL_STAIRS,
                            start.getX(),start.getY(),start.getZ(),Math.max(1,count*Math.abs(d[0])),count,Math.max(1,count*Math.abs(d[1])));
            }
        }
    }

    private static void detectDripstone(Map<Long,BlockState> blocks, BlockPos p, DripstoneESPModule module,
                                        List<StructureTarget> out, Set<Long> emitted) {
        boolean hasAbove = get(blocks,p.above()) != null && get(blocks,p.above()).is(Blocks.POINTED_DRIPSTONE);
        boolean hasBelow = get(blocks,p.below()) != null && get(blocks,p.below()).is(Blocks.POINTED_DRIPSTONE);
        if (!hasAbove && hasBelow) {
            int length=1;
            while(length<24 && get(blocks,p.below(length))!=null && get(blocks,p.below(length)).is(Blocks.POINTED_DRIPSTONE)) length++;
            if(solid(blocks,p.above()) && length>=module.minimumStalactiteLength())
                add(out,emitted,StructureESPModule.Detector.DRIPSTONE,p.getX(),p.getY()-length+1,p.getZ(),1,length,1);
        }
        if (!hasBelow && hasAbove) {
            int length=1;
            while(length<24 && get(blocks,p.above(length))!=null && get(blocks,p.above(length)).is(Blocks.POINTED_DRIPSTONE)) length++;
            if(solid(blocks,p.below()) && length>=module.minimumStalagmiteLength())
                add(out,emitted,StructureESPModule.Detector.DRIPSTONE,p.getX(),p.getY(),p.getZ(),1,length,1);
        }
    }

    private static void detectChunks(Map<Long,BlockState> blocks, ChunkFinderModule module,
                                     List<StructureTarget> out, Set<Long> emitted, Minecraft client) {
        Map<Long,Integer> score=new HashMap<>();
        for(var e:blocks.entrySet()){
            BlockState s=e.getValue(); int value=0;
            if(s.is(Blocks.COBBLESTONE)||s.is(Blocks.MOSSY_COBBLESTONE)) value=1;
            else if(s.is(Blocks.DEEPSLATE_BRICKS)||s.is(Blocks.POLISHED_DEEPSLATE)||s.is(Blocks.DEEPSLATE_TILES)) value=2;
            else if(s.is(Blocks.OBSIDIAN)||s.is(Blocks.REPEATER)||s.is(Blocks.PISTON)||s.is(Blocks.STICKY_PISTON)) value=3;
            else if(s.is(Blocks.CHEST)||s.is(Blocks.BARREL)||s.is(Blocks.HOPPER)) value=4;
            if(value>0){BlockPos p=BlockPos.of(e.getKey());long chunk=(((long)(p.getX()>>4))<<32)^((p.getZ()>>4)&0xffffffffL);score.merge(chunk,value,Integer::sum);}
        }
        for(var e:score.entrySet()) if(e.getValue()>=module.minimumScore()){
            int cx=(int)(e.getKey()>>32),cz=(int)(long)e.getKey();
            add(out,emitted,StructureESPModule.Detector.CHUNK_FINDER,cx<<4,client.player.blockPosition().getY(),cz<<4,16,1,16);
        }
    }

    private static boolean enclosedAir(Map<Long,BlockState> b,BlockPos p) {
        return solid(b,p.above())&&solid(b,p.below())&&solid(b,p.north())&&solid(b,p.south())&&solid(b,p.east())&&solid(b,p.west());
    }
    private static boolean cardinalWalls(Map<Long,BlockState>b,BlockPos p){return solid(b,p.north())&&solid(b,p.south())&&solid(b,p.east())&&solid(b,p.west());}
    private static int verticalAirDepth(Map<Long,BlockState>b,BlockPos p,int max){int n=0;while(n<max&&passable(b,p.below(n)))n++;return n;}
    private static boolean likelyPlacedCover(BlockState s){String id=s.getBlock().builtInRegistryHolder().key().location().getPath();return id.contains("cobblestone")||id.contains("stone_brick")||id.contains("planks")||id.contains("log")||id.contains("wool")||id.contains("concrete")||id.contains("terracotta")||id.contains("glass");}
    private static boolean passable(Map<Long,BlockState>b,BlockPos p){BlockState s=get(b,p);return s!=null&&s.isAir();}
    private static boolean solid(Map<Long,BlockState>b,BlockPos p){BlockState s=get(b,p);return s!=null&&!s.isAir()&&s.blocksMotion();}
    private static BlockState get(Map<Long,BlockState>b,BlockPos p){return b.get(p.asLong());}
    private static void add(List<StructureTarget>out,Set<Long>emitted,StructureESPModule.Detector type,double x,double y,double z,double w,double h,double d){
        long key=BlockPos.asLong((int)x,(int)y,(int)z)^((long)type.ordinal()<<56);
        if(emitted.add(key))out.add(new StructureTarget(type,x,y,z,x+w,y+h,z+d));
    }
    private static int signature(List<StructureESPModule> modules){int h=1;for(var m:modules){h=31*h+m.id().hashCode();h=31*h+Boolean.hashCode(m.enabled());if(m.enabled())h=31*h+Double.hashCode(m.range());}return h;}
}
