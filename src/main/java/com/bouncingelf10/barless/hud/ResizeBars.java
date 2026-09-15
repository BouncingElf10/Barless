package com.bouncingelf10.barless.hud;

import com.bouncingelf10.barless.WindowDragLock;
import com.bouncingelf10.barless.mixin.accessor.WindowAccessor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLVideo;

public class ResizeBars {

    private static final int EDGE = 4;
    private static final int CORNER = 10;
    private static final int BAR_COLOR = 0x55FFFFFF;

    private static final CursorType CURSOR_NWSE =
            CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_NWSE_RESIZE, "barless_resize_nwse", CursorTypes.ARROW);
    private static final CursorType CURSOR_NESW =
            CursorType.createStandardCursor(SDLMouse.SDL_SYSTEM_CURSOR_NESW_RESIZE, "barless_resize_nesw", CursorTypes.ARROW);

    private enum Zone {
        NONE, LEFT, RIGHT, TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private static Zone hovered = Zone.NONE;
    private static boolean resizing = false;

    private static double startMouseX, startMouseY;
    private static int startWinX, startWinY;
    private static int startWinW, startWinH;

    private static boolean hasPending = false;
    private static int pendingX, pendingY, pendingW, pendingH;
    private static Zone resizeZone = Zone.NONE;
    private static boolean prevMouseDown = false;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        WindowAccessor acc = (WindowAccessor) (Object) window;
        long handle = acc.barless$getHandle();

        if (acc.barless$isFullscreen() || mc.mouseHandler.isMouseGrabbed()) {
            cancelResize();
            return;
        }

        int winW = window.getScreenWidth();
        int winH = window.getScreenHeight();

        double[] c = Sdl.cursorPos();

        updateHover(winW, winH, c[0], c[1]);
        updateCursor(window);
        calculateResize(handle, winW, winH, c[0], c[1]);
    }

    private static void cancelResize() {
        if (resizing) {
            WindowDragLock.release(WindowDragLock.Owner.RESIZE);
        }
        resizing = false;
        resizeZone = Zone.NONE;
        hovered = Zone.NONE;
        hasPending = false;
        prevMouseDown = Sdl.leftMouseDown();
    }

    private static void updateHover(int w, int h, double mx, double my) {
        boolean left = mx <= EDGE;
        boolean right = mx >= w - EDGE;
        boolean top = my <= EDGE;
        boolean bottom = my >= h - EDGE;

        hovered = Zone.NONE;

        if (top && left) hovered = Zone.TOP_LEFT;
        else if (top && right) hovered = Zone.TOP_RIGHT;
        else if (bottom && left) hovered = Zone.BOTTOM_LEFT;
        else if (bottom && right) hovered = Zone.BOTTOM_RIGHT;

        else if (left) hovered = Zone.LEFT;
        else if (right) hovered = Zone.RIGHT;
        else if (top) hovered = Zone.TOP;
        else if (bottom) hovered = Zone.BOTTOM;
    }

    private static void updateCursor(Window window) {
        if (resizing) return;
        if (WindowDragLock.isHeldBy(WindowDragLock.Owner.MOVE)) return;

        window.selectCursor(switch (hovered) {
            case LEFT, RIGHT -> CursorTypes.RESIZE_EW;
            case TOP, BOTTOM -> CursorTypes.RESIZE_NS;
            case TOP_LEFT, BOTTOM_RIGHT -> CURSOR_NWSE;
            case TOP_RIGHT, BOTTOM_LEFT -> CURSOR_NESW;
            default -> CursorTypes.ARROW;
        });
    }

    public static void applyPendingResize() {
        if (!hasPending) return;
        hasPending = false;

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        WindowAccessor acc = (WindowAccessor) (Object) window;
        long handle = ((WindowAccessor) (Object) window).barless$getHandle();

        SDLVideo.SDL_SetWindowPosition(handle, pendingX, pendingY);
        SDLVideo.SDL_SetWindowSize(handle, pendingW, pendingH);

        int[] fb = Sdl.framebufferSize(handle);

        acc.barless$setWidth(pendingW);
        acc.barless$setHeight(pendingH);
        acc.barless$setFramebufferWidth(fb[0]);
        acc.barless$setFramebufferHeight(fb[1]);
        acc.barless$setIsResized(true);

        mc.resizeGui();
    }

    private static void calculateResize(long handle, int winW, int winH, double mx, double my) {
        boolean down = Sdl.leftMouseDown();

        if (down) {
            int[] w = Sdl.windowPos(handle);
            double screenMX = w[0] + mx;
            double screenMY = w[1] + my;

            if (!resizing && hovered != Zone.NONE && !prevMouseDown) {
                if (WindowDragLock.tryAcquire(WindowDragLock.Owner.RESIZE)) {
                    resizing = true;
                    resizeZone = hovered;
                    startMouseX = screenMX;
                    startMouseY = screenMY;
                    startWinX = w[0];
                    startWinY = w[1];
                    startWinW = winW;
                    startWinH = winH;
                }
            }

            if (resizing && WindowDragLock.isHeldBy(WindowDragLock.Owner.RESIZE)) {
                double dx = screenMX - startMouseX;
                double dy = screenMY - startMouseY;

                int newX = startWinX;
                int newY = startWinY;
                int newW = startWinW;
                int newH = startWinH;

                switch (resizeZone) {
                    case RIGHT -> newW = (int) (startWinW + dx);
                    case LEFT -> {
                        newW = (int) (startWinW - dx);
                        newX = (int) (startWinX + dx);
                    }
                    case BOTTOM -> newH = (int) (startWinH + dy);
                    case TOP -> {
                        newH = (int) (startWinH - dy);
                        newY = (int) (startWinY + dy);
                    }
                    case TOP_LEFT -> {
                        newW = (int) (startWinW - dx);
                        newH = (int) (startWinH - dy);
                        newX = (int) (startWinX + dx);
                        newY = (int) (startWinY + dy);
                    }
                    case TOP_RIGHT -> {
                        newW = (int) (startWinW + dx);
                        newH = (int) (startWinH - dy);
                        newY = (int) (startWinY + dy);
                    }
                    case BOTTOM_LEFT -> {
                        newW = (int) (startWinW - dx);
                        newH = (int) (startWinH + dy);
                        newX = (int) (startWinX + dx);
                    }
                    case BOTTOM_RIGHT -> {
                        newW = (int) (startWinW + dx);
                        newH = (int) (startWinH + dy);
                    }
                }

                newW = Math.max(newW, 100);
                newH = Math.max(newH, 80);

                pendingX = newX;
                pendingY = newY;
                pendingW = newW;
                pendingH = newH;
                hasPending = true;

                startMouseX = screenMX;
                startMouseY = screenMY;
                startWinX = newX;
                startWinY = newY;
                startWinW = newW;
                startWinH = newH;
            }
        } else {
            if (resizing) {
                WindowDragLock.release(WindowDragLock.Owner.RESIZE);
            }
            resizing = false;
        }

        prevMouseDown = down;
    }
}