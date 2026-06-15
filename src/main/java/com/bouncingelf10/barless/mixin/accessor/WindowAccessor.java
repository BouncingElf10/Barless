package com.bouncingelf10.barless.mixin.accessor;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Window.class)
public interface WindowAccessor {
    @Accessor("handle")
    long barless$getHandle();

    @Invoker("setWindowed")
    void barless$setWindowed(int width, int height);

    @Accessor("framebufferWidth")
    void barless$setFramebufferWidth(int w);

    @Accessor("framebufferHeight")
    void barless$setFramebufferHeight(int h);

    @Accessor("width")
    void barless$setWidth(int w);

    @Accessor("height")
    void barless$setHeight(int h);

    @Accessor("dirty")
    void barless$setDirty(boolean v);
}
