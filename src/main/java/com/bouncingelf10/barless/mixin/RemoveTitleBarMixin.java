package com.bouncingelf10.barless.mixin;

import com.bouncingelf10.barless.mixin.accessor.WindowHandleAccessor;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class RemoveTitleBarMixin {
	@Inject(at = @At("HEAD"), method = "run")
	private void init(CallbackInfo info) {
		Minecraft mc = (Minecraft)(Object)this;
		Window window = mc.getWindow();
		long handle = ((WindowHandleAccessor)(Object) window).getHandle();

		GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
	}
}