package com.judiraal.irisblockcompat.mixin;

import com.judiraal.irisblockcompat.IrisBlockCompatConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Iris.class)
public class IrisMixin {
    @ModifyExpressionValue(method = "createPipeline", at = @At(value = "FIELD", target = "net/irisshaders/iris/Iris.currentPack:Lnet/irisshaders/iris/shaderpack/ShaderPack;"))
    private static ShaderPack irisblockcompat$filterDimension(ShaderPack original, @Local(argsOnly = true) NamespacedId dimensionId) {
        if (original == null) return null;
        if (IrisBlockCompatConfig.disabledDimensions.get().contains(dimensionId.toString())) return null;
        return original;
    }
}
