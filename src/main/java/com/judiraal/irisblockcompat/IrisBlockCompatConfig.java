package com.judiraal.irisblockcompat;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;

public class IrisBlockCompatConfig {
    public static final IrisBlockCompatConfig CONFIG;
    static final ModConfigSpec SPEC;

    static {
        Pair<IrisBlockCompatConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(IrisBlockCompatConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    public static ModConfigSpec.ConfigValue<List<? extends String>> wavingBlocks;
    public static ModConfigSpec.ConfigValue<List<? extends String>> leavesBlocks;
    public static ModConfigSpec.ConfigValue<List<? extends String>> cropsBlocks;
    public static ModConfigSpec.ConfigValue<List<? extends String>> excludeBlocks;
    public static Lazy<Object2IntMap<BlockState>> compatBlockStates;

    private IrisBlockCompatConfig(final ModConfigSpec.Builder builder) {
        wavingBlocks = builder.comment("List of blocks and/or block tags that will be included as waving grass-like.")
                .define("wavingBlocks", () -> Arrays.asList("#minecraft:sword_efficient", "farmersdelight:sandy_shrub"), value -> true);
        leavesBlocks = builder.comment("List of blocks and/or block tags that will be included as leaves.")
                .define("leavesBlocks", () -> Arrays.asList("#minecraft:leaves"), value -> true);
        cropsBlocks = builder.comment("List of blocks and/or block tags that will be included as crops.")
                .define("cropsBlocks", () -> Arrays.asList(), value -> true);
        excludeBlocks = builder.comment("List of blocks and/or block tags that will be excluded from the other lists.")
                .define("excludeBlocks", () -> Arrays.asList("#minecraft:saplings",
                        "minecraft:melon", "minecraft:pumpkin", "minecraft:carved_pumpkin",
                        "minecraft:jack_o_lantern", "minecraft:chorus_plant", "minecraft:chorus_flower"), value -> true);
        compatBlockStates = Lazy.of(this::buildCompatBlockStates);

        builder.build();
    }

    private <T extends String> List<Block> blockListToBlocks(List<T> blockList) {
        var result = new ArrayList<Block>();
        Registry<Block> blockRegistry = BuiltInRegistries.BLOCK;
        for (var blockString : blockList) {
            if (blockString.startsWith("#")) {
                ResourceLocation tagLocation = ResourceLocation.parse(blockString.substring(1));
                TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagLocation);
                blockRegistry.getTagOrEmpty(tagKey).forEach(blockHolder -> result.add(blockHolder.value()));
            } else {
                Block block = blockRegistry.get(ResourceLocation.parse(blockString));
                if (block != Blocks.AIR) result.add(block);
            }
        }
        return result;
    }

    private Object2IntMap<BlockState> buildCompatBlockStates() {
        var result = new Object2IntLinkedOpenHashMap<BlockState>();
        var excluded = new HashSet<>(blockListToBlocks(excludeBlocks.get()));
        for (var b: blockListToBlocks(wavingBlocks.get())) {
            if (excluded.contains(b)) continue;
            BlockState state = b.defaultBlockState();
            if (state.hasProperty(DOUBLE_BLOCK_HALF)) {
                result.put(state.setValue(DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), 10175);
                result.put(state.setValue(DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 10176);
            } else {
                result.put(state, 10031);
            }
        }
        for (var b: blockListToBlocks(leavesBlocks.get())) {
            if (excluded.contains(b)) continue;
            for (BlockState s: b.getStateDefinition().getPossibleStates())
                result.put(s, 10018);
        }
        for (var b: blockListToBlocks(cropsBlocks.get())) {
            if (excluded.contains(b)) continue;
            for (BlockState s: b.getStateDefinition().getPossibleStates())
                result.put(s, 10059);
        }
        return result;
    }
}
