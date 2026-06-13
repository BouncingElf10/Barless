package com.bouncingelf10.barless.mixin;

import com.bouncingelf10.barless.hud.ResizeBars;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onRenderHead(CallbackInfo ci) {
        ResizeBars.applyPendingResize();
    }
}
