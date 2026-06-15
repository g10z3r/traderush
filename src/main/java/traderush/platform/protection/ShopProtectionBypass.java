package traderush.platform.protection;

/**
 * Allows trusted mod code (e.g. shop generation) to place blocks inside a
 * protected shop area.
 */
public final class ShopProtectionBypass {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal
            .withInitial(() -> false);

    private ShopProtectionBypass() {}

    public static void run(Runnable action) {
        ACTIVE.set(true);
        try {
            action.run();
        } finally {
            ACTIVE.remove();
        }
    }

    public static boolean isActive() {
        return ACTIVE.get();
    }
}
