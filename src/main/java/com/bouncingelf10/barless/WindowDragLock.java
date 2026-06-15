package com.bouncingelf10.barless;

public final class WindowDragLock {
    public enum Owner { NONE, MOVE, RESIZE }

    private static Owner current = Owner.NONE;

    public static boolean tryAcquire(Owner requester) {
        if (current == Owner.NONE) {
            current = requester;
            return true;
        }
        return current == requester;
    }

    public static void release(Owner requester) {
        if (current == requester) {
            current = Owner.NONE;
        }
    }

    public static boolean isHeldBy(Owner requester) {
        return current == requester;
    }

    public static boolean isAvailable() {
        return current == Owner.NONE;
    }
}