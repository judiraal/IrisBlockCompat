package com.judiraal.irisblockcompat.mixin;

import com.judiraal.irisblockcompat.IrisBlockCompatConfig;
import com.judiraal.irisblockcompat.gui.DimensionOverrideButton;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApiConfig;
import net.irisshaders.iris.config.IrisConfig;
import net.irisshaders.iris.gui.element.screen.IrisButton;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.uniforms.transforms.SmoothedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderPackScreen.class)
public class ShaderPackScreenMixin extends Screen {
    @Final
    @Shadow
    public SmoothedFloat buttonTransition;
    @Shadow
    private boolean optionMenuOpen;
    @Unique
    private DimensionOverrideButton dimensionButton;

    protected ShaderPackScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "net/irisshaders/iris/gui/screen/ShaderPackScreen.refreshScreenSwitchButton ()V", shift = At.Shift.AFTER))
    private void irisblockcompat$addDimensionButton(CallbackInfo ci) {
        this.dimensionButton = null;
        if (!optionMenuOpen && Minecraft.getInstance().level != null && IrisBlockCompatConfig.enablePerDimensionShaders.get())
            addRenderableWidget(this.dimensionButton = new DimensionOverrideButton(this.width - 160, 7, 152, 20, buttonTransition));
    }

    @Inject(method = "applyChanges", at = @At("HEAD"))
    private void irisblockcompat$applyChangesEnabled(CallbackInfo ci) {
        if (dimensionButton != null) {
            var settings = IrisBlockCompatConfig.getDimensionSettings();
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            var dimensionId = level.dimension().location();
            if (dimensionButton.useDimensionOverride() && settings.isEmpty()) {
                IrisBlockCompatConfig.setDimensionSettings(dimensionId, Iris.getIrisConfig().areShadersEnabled(), Iris.getIrisConfig().getShaderPackName().orElse(null));
            } else if (!dimensionButton.useDimensionOverride() && settings.isPresent()) {
                IrisBlockCompatConfig.removeDimensionSettings(dimensionId);
                Iris.getIrisConfig().setShadersEnabled(settings.get().enabled());
                Iris.getIrisConfig().setShaderPackName(settings.get().shaderPackName());
            }
        }
    }
}
