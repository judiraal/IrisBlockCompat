package com.judiraal.irisblockcompat.mixin;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.config.IrisConfig;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Iris.class)
public abstract class IrisMixin {
    @Shadow
    private static ShaderPack currentPack;
    @Shadow
    private static IrisConfig irisConfig;
    @Shadow
    private static String currentPackName;

    @Shadow
    private static void destroyEverything() {}

    @Shadow
    public static void loadShaderpack() {}

    @Unique
    private static void irisblockcompat$dimensionSpecificShader() {
        destroyEverything();
        loadShaderpack();
    }

    @Inject(method = "createPipeline", at = @At("HEAD"))
    private static void irisblockcompat$filterDimension(NamespacedId dimensionId, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        Optional<String> packName = irisConfig.getShaderPackName();
        if (currentPack != null || packName.isPresent()) {
            if (currentPack == null || packName.isEmpty()) irisblockcompat$dimensionSpecificShader();
            else if (!packName.get().equals(currentPackName)) irisblockcompat$dimensionSpecificShader();
        }
    }
}
