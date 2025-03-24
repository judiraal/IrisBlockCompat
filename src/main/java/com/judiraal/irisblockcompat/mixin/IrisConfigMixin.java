package com.judiraal.irisblockcompat.mixin;

import com.judiraal.irisblockcompat.IrisBlockCompat;
import com.judiraal.irisblockcompat.IrisBlockCompatConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.irisshaders.iris.config.IrisConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(IrisConfig.class)
public abstract class IrisConfigMixin {
    @Shadow private String shaderPackName;
    @Shadow public abstract Optional<String> getShaderPackName();

    @Inject(method = "getShaderPackName", at = @At("HEAD"), cancellable = true)
    private void irisblockcompat$dimensionShaderPack(CallbackInfoReturnable<Optional<String>> cir) {
        var settings = IrisBlockCompatConfig.getDimensionSettings();
        if (settings.isEmpty()) return;
        if (!settings.get().enabled()) cir.setReturnValue(Optional.empty());
        cir.setReturnValue(Optional.ofNullable(settings.get().shaderPackName()));
    }

    @Inject(method = "areShadersEnabled", at = @At("RETURN"), cancellable = true)
    private void irisblockcompat$dimensionShaderPackEnabled(CallbackInfoReturnable<Boolean> cir) {
        var settings = IrisBlockCompatConfig.getDimensionSettings();
        if (settings.isEmpty()) return;
        if (settings.get().enabled() != cir.getReturnValue())
            cir.setReturnValue(settings.get().enabled());
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "net/irisshaders/iris/config/IrisConfig.getShaderPackName ()Ljava/util/Optional;"))
    private Optional<String> irisblockcompat$saveDefaultShaderPack(IrisConfig instance) {
        return Optional.ofNullable(shaderPackName);
    }

    @Inject(method = "setShadersEnabled", at = @At("HEAD"), cancellable = true)
    private void irisblockcompat$dimensionShaderPackEnabled(boolean enabled, CallbackInfo ci) {
        var settings = IrisBlockCompatConfig.getDimensionSettings();
        if (settings.isPresent()) {
            IrisBlockCompatConfig.setDimensionSettings(Minecraft.getInstance().level.dimension().location(), enabled, settings.get().shaderPackName());
            ci.cancel();
        }
    }

    @Inject(method = "setShaderPackName", at = @At("HEAD"), cancellable = true)
    private void irisblockcompat$dimensionShaderPack(String name, CallbackInfo ci) {
        var settings = IrisBlockCompatConfig.getDimensionSettings();
        if (settings.isPresent()) {
            IrisBlockCompatConfig.setDimensionSettings(Minecraft.getInstance().level.dimension().location(), settings.get().enabled(), name);
            ci.cancel();
        }
    }
}
