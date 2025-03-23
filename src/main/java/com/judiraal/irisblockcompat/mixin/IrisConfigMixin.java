package com.judiraal.irisblockcompat.mixin;

import com.judiraal.irisblockcompat.IrisBlockCompat;
import net.irisshaders.iris.config.IrisConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(IrisConfig.class)
public class IrisConfigMixin {
    @Inject(method = "getShaderPackName", at = @At("HEAD"), cancellable = true)
    private void irisblockcompat$dimensionShaderPack(CallbackInfoReturnable<Optional<String>> cir) {
        IrisBlockCompat.setDimensionShaderPack(cir);
    }
}
