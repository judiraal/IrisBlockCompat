package com.judiraal.irisblockcompat.mixin;

import com.judiraal.irisblockcompat.IrisBlockCompat;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.shaderpack.materialmap.BlockEntry;
import net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.TagEntry;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockMaterialMapping.class)
public class BlockMaterialMappingMixin {
    @Inject(method = "createBlockStateIdMap", at = @At("RETURN"))
    private static void irisblockcompat$addCompatBlockIds(Int2ObjectLinkedOpenHashMap<List<BlockEntry>> blockPropertiesMap, Int2ObjectLinkedOpenHashMap<List<TagEntry>> tagPropertiesMap, CallbackInfoReturnable<Object2IntMap<BlockState>> cir) {
        IrisBlockCompat.addCompatBlockIds(cir.getReturnValue());
    }
}
