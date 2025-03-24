package com.judiraal.irisblockcompat;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(IrisBlockCompat.MOD_ID)
public class IrisBlockCompat {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "irisblockcompat";

    public IrisBlockCompat(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, IrisBlockCompatConfig.SPEC);
    }

    public static void addCompatBlockIds(Object2IntMap<BlockState> blockIdMap) {
        IrisBlockCompatConfig.compatBlockStates.get().object2IntEntrySet()
                .forEach(e -> blockIdMap.putIfAbsent(e.getKey(), e.getIntValue()));
    }
}
