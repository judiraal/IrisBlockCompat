package com.judiraal.irisblockcompat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.judiraal.irisblockcompat.util.DimensionSettings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.JsonUtils;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;

@EventBusSubscriber(modid = IrisBlockCompat.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class IrisBlockCompatConfig {
    public static final IrisBlockCompatConfig CONFIG;
    static final ModConfigSpec SPEC;

    static {
        Pair<IrisBlockCompatConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(IrisBlockCompatConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    private static final Path SETTINGS_CONFIG = FMLPaths.CONFIGDIR.get().resolve("irisblockcompat-dims.json");
    private static final Codec<Map<ResourceLocation, DimensionSettings>> SETTINGS_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, DimensionSettings.CODEC);

    public static ModConfigSpec.ConfigValue<List<? extends String>> wavingBlocks;
    public static ModConfigSpec.ConfigValue<List<? extends String>> leavesBlocks;
    public static ModConfigSpec.ConfigValue<List<? extends String>> cropsBlocks;
    public static ModConfigSpec.ConfigValue<List<? extends String>> excludeBlocks;
    public static ModConfigSpec.ConfigValue<List<? extends String>> disabledDimensions;
    public static ModConfigSpec.BooleanValue enablePerDimensionShaders;
    public static Lazy<Object2IntMap<BlockState>> compatBlockStates;
    public static Map<ResourceLocation, DimensionSettings> dimensionShaderPacks = ImmutableMap.of();

    private IrisBlockCompatConfig(final ModConfigSpec.Builder builder) {
        wavingBlocks = builder.comment("List of blocks and/or block tags that will be included as waving grass-like.")
                .defineList("wavingBlocks", () -> Arrays.asList("#minecraft:sword_efficient", "farmersdelight:sandy_shrub"), () -> "", value -> true);
        leavesBlocks = builder.comment("List of blocks and/or block tags that will be included as leaves.")
                .defineList("leavesBlocks", () -> Arrays.asList("#minecraft:leaves"), () -> "", value -> true);
        cropsBlocks = builder.comment("List of blocks and/or block tags that will be included as crops.")
                .defineList("cropsBlocks", () -> Arrays.asList(""), () -> "", value -> true);
        excludeBlocks = builder.comment("List of blocks and/or block tags that will be excluded from the other lists.")
                .defineList("excludeBlocks", () -> Arrays.asList("#minecraft:saplings",
                        "minecraft:melon", "minecraft:pumpkin", "minecraft:carved_pumpkin",
                        "minecraft:jack_o_lantern", "minecraft:chorus_plant", "minecraft:chorus_flower"), () -> "", value -> true);
        disabledDimensions = builder.comment("List of dimensions where shaders should not be used.")
                .defineList("disabledDimensions", () -> Arrays.asList(
                        "stellaris:moon", "stellaris:mars", "stellaris:venus", "stellaris:mercury"), () -> "", value -> true);
        enablePerDimensionShaders = builder.comment("Enable optionally specifying different shader settings per dimension.")
                .define("enablePerDimensionShaders", true);
        compatBlockStates = Lazy.of(this::buildCompatBlockStates);

        builder.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        loadDimensionSettings();
    }

    public static Optional<DimensionSettings> getDimensionSettings() {
        var level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();
        return getDimensionSettings(level.dimension().location());
    }

    public static Optional<DimensionSettings> getDimensionSettings(ResourceLocation dimensionId) {
        if (!enablePerDimensionShaders.get()) return Optional.empty();
        return Optional.ofNullable(dimensionShaderPacks.get(dimensionId));
    }

    public static synchronized void removeDimensionSettings(ResourceLocation dimensionId) {
        if (!dimensionShaderPacks.containsKey(dimensionId)) return;
        dimensionShaderPacks = ImmutableMap.copyOf(Maps.filterKeys(dimensionShaderPacks, k -> !dimensionId.equals(k)));
        saveDimensionSettings();
    }

    public static synchronized void setDimensionSettings(ResourceLocation dimensionId, boolean enabled, String shaderPackName) {
        var map = new LinkedHashMap<>(dimensionShaderPacks);
        map.put(dimensionId, new DimensionSettings(enabled, shaderPackName));
        dimensionShaderPacks = ImmutableMap.copyOf(map);
        saveDimensionSettings();
    }

    private static void saveDimensionSettings() {
        try {
            var json = SETTINGS_CODEC.encodeStart(JsonOps.INSTANCE, dimensionShaderPacks)
                .resultOrPartial(error -> IrisBlockCompat.LOGGER.info("Encoding error: " + error))
                .orElse(null);
            if (json != null) Files.writeString(SETTINGS_CONFIG, new GsonBuilder().setPrettyPrinting().create().toJson(json));
        } catch (Exception ignored) {}
    }

    private static void loadDimensionSettings() {
        if (Files.exists(SETTINGS_CONFIG)) try {
            var json = new Gson().fromJson(Files.readString(SETTINGS_CONFIG), JsonElement.class);
            dimensionShaderPacks = ImmutableMap.copyOf(SETTINGS_CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> IrisBlockCompat.LOGGER.info("Decoding error: " + error))
                    .orElse(ImmutableMap.of()));
        } catch (Exception ignored) {}
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
