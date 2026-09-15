package com.bouncingelf10.barless.hud;

import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class Sdl {
    private Sdl() {}

    public static double[] cursorPos() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer x = stack.mallocFloat(1);
            FloatBuffer y = stack.mallocFloat(1);
            SDLMouse.SDL_GetMouseState(x, y);
            return new double[]{x.get(0), y.get(0)};
        }
    }

    public static boolean leftMouseDown() {
        return (SDLMouse.nSDL_GetMouseState(MemoryUtil.NULL, MemoryUtil.NULL) & SDLMouse.SDL_BUTTON_LMASK) != 0;
    }

    public static int[] windowPos(long window) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            SDLVideo.SDL_GetWindowPosition(window, x, y);
            return new int[]{x.get(0), y.get(0)};
        }
    }

    public static int[] framebufferSize(long window) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            SDLVideo.SDL_GetWindowSizeInPixels(window, w, h);
            return new int[]{w.get(0), h.get(0)};
        }
    }
}
