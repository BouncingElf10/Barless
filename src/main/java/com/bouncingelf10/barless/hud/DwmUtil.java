package com.bouncingelf10.barless.hud;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.lwjgl.glfw.GLFWNativeWin32;

public class DwmUtil {
    public interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);

        void DwmExtendFrameIntoClientArea(WinDef.HWND hwnd, MARGINS pMarInset);
        void DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, WinDef.DWORDByReference pvAttribute, int cbAttribute);
    }

    @Structure.FieldOrder({"cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight"})
    public static class MARGINS extends Structure {
        public int cxLeftWidth;
        public int cxRightWidth;
        public int cyTopHeight;
        public int cyBottomHeight;

        public MARGINS(int all) {
            this.cxLeftWidth = all;
            this.cxRightWidth = all;
            this.cyTopHeight = all;
            this.cyBottomHeight = all;
        }
    }

    private static final int DWMWA_WINDOW_CORNER_PREFERENCE = 33;
    private static final int DWMWCP_DONOTROUND = 1;
    private static final int DWMWCP_ROUND = 2;

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public static void applyRoundedCornersAndShadow(long glfwHandle) {
        setRoundedCornersAndShadow(glfwHandle, true);
    }

    public static void setRoundedCornersAndShadow(long glfwHandle, boolean enabled) {
        if (!isWindows()) return;

        long hwndLong = GLFWNativeWin32.glfwGetWin32Window(glfwHandle);
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(hwndLong));

        int preference = enabled ? DWMWCP_ROUND : DWMWCP_DONOTROUND;
        WinDef.DWORDByReference corner = new WinDef.DWORDByReference(new WinDef.DWORD(preference));
        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, corner, 4);
        Dwmapi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, new MARGINS(enabled ? -1 : 0));
    }
}