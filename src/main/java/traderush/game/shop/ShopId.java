package traderush.game.shop;

import java.util.UUID;

public record ShopId(UUID value) {
    public static ShopId fromUuid(UUID value) {
        return new ShopId(value);
    }

    public static ShopId fromString(String value) {
        return new ShopId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
