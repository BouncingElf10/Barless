package com.bouncingelf10.barless.hud;

import com.bouncingelf10.barless.WindowDragLock;
import com.bouncingelf10.barless.mixin.accessor.WindowAccessor;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ResizeBars {

    private static final int EDGE = 4;
    private static final int CORNER = 10;
    private static final int BAR_COLOR = 0x55FFFFFF;

    private static final long cursorArrow;
    private static final long cursorH;
    private static final long cursorV;
    private static final long cursorNWSE;
    private static final long cursorNESW;

    static {
        cursorArrow = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
        cursorH = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
        cursorV = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR);
        cursorNWSE = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR);
        cursorNESW = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR);
    }

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
    private static int prevMouseState = GLFW.GLFW_RELEASE;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        long handle = ((WindowAccessor) (Object) window).barless$getHandle();

        if (window.isFullscreen() || mc.mouseHandler.isMouseGrabbed()) {
            cancelResize(handle);
            return;
        }

        int winW = window.getScreenWidth();
        int winH = window.getScreenHeight();

        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(handle, mx, my);

        updateHover(handle, winW, winH, mx[0], my[0]);
        updateCursor(handle);
        calculateResize(handle, winW, winH, mx[0], my[0]);
    }

    private static void cancelResize(long handle) {
        if (resizing) {
            WindowDragLock.release(WindowDragLock.Owner.RESIZE);
        }
        resizing = false;
        resizeZone = Zone.NONE;
        hovered = Zone.NONE;
        hasPending = false;
        prevMouseState = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    private static void updateHover(long handle, int w, int h, double mx, double my) {
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

    private static void updateCursor(long handle) {
        if (resizing) return;
        if (WindowDragLock.isHeldBy(WindowDragLock.Owner.MOVE)) return;

        switch (hovered) {
            case LEFT, RIGHT -> GLFW.glfwSetCursor(handle, cursorH);
            case TOP, BOTTOM -> GLFW.glfwSetCursor(handle, cursorV);
            case TOP_LEFT, BOTTOM_RIGHT -> GLFW.glfwSetCursor(handle, cursorNWSE);
            case TOP_RIGHT, BOTTOM_LEFT -> GLFW.glfwSetCursor(handle, cursorNESW);
            default -> GLFW.glfwSetCursor(handle, cursorArrow);
        }
    }

    public static void applyPendingResize() {
        if (!hasPending) return;
        hasPending = false;

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        WindowAccessor acc = (WindowAccessor) (Object) window;
        long handle = ((WindowAccessor) (Object) window).barless$getHandle();

        GLFW.glfwSetWindowPos(handle, pendingX, pendingY);
        GLFW.glfwSetWindowSize(handle, pendingW, pendingH);

        int[] fbW = new int[1], fbH = new int[1];
        GLFW.glfwGetFramebufferSize(handle, fbW, fbH);

        acc.barless$setWidth(pendingW);
        acc.barless$setHeight(pendingH);
        acc.barless$setFramebufferWidth(fbW[0]);
        acc.barless$setFramebufferHeight(fbH[0]);
        acc.barless$setDirty(true);

        mc.resizeDisplay();
    }

    private static void calculateResize(long handle, int winW, int winH, double mx, double my) {
        int state = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT);

        if (state == GLFW.GLFW_PRESS) {
            int[] wx = new int[1], wy = new int[1];
            GLFW.glfwGetWindowPos(handle, wx, wy);
            double screenMX = wx[0] + mx;
            double screenMY = wy[0] + my;

            if (!resizing && hovered != Zone.NONE && prevMouseState == GLFW.GLFW_RELEASE) {
                if (WindowDragLock.tryAcquire(WindowDragLock.Owner.RESIZE)) {
                    resizing = true;
                    resizeZone = hovered;
                    startMouseX = screenMX;
                    startMouseY = screenMY;
                    startWinX = wx[0];
                    startWinY = wy[0];
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

        prevMouseState = state;
    }
}